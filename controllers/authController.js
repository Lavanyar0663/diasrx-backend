const asyncHandler = require("express-async-handler");
const bcrypt = require("bcrypt");
const db = require("../db");
const authService = require("../services/authService");
const emailService = require("../services/emailService");
const { withTransaction } = require("../utils/dbHelper");

const normalizeName = (name, role) => {
    if (!name) return name;
    let cleanName = name.trim();
    if (role === 'doctor') {
        // Strip prefixes like "Dr", "Dr.", "Dr. . ", and any leading dots or spaces
        cleanName = cleanName.replace(/^(dr|dr\.)\s*\.*\s*/i, "");
        return `Dr. ${cleanName}`;
    } else if (role === 'pharmacist') {
        // Strip prefixes like "Ph", "Ph.", "Ph. . ", and any leading dots or spaces
        cleanName = cleanName.replace(/^(ph|ph\.)\s*\.*\s*/i, "");
        return `Ph. ${cleanName}`;
    }
    return cleanName;
};

exports.login = asyncHandler(async (req, res) => {
    const { email, password, role } = req.body;

    if (!email || !password) {
        const err = new Error("Email and password are required");
        err.status = 400;
        throw err;
    }

    const result = await new Promise((resolve, reject) => {
        const sql = `
            SELECT u.*, 
                   COALESCE(d.phone, ph.phone) as phone,
                   COALESCE(d.professional_title, ph.professional_title) as professional_title,
                   COALESCE(d.specialization, ph.specialization) as department
            FROM users u
            LEFT JOIN doctors d ON u.id = d.user_id
            LEFT JOIN pharmacists ph ON u.id = ph.user_id
            WHERE u.email = ?`;
        db.query(sql, [email.toLowerCase().trim()], (err, rows) => {
            if (err) return reject(err);
            resolve(rows);
        });
    });

    if (result.length === 0) {
        const err = new Error("User not found");
        err.status = 404;
        throw err;
    }

    const user = result[0];

    // 1. Validate role match
    if (role && user.role !== role.toLowerCase()) {
        const err = new Error("Selected role does not match account");
        err.status = 403;
        throw err;
    }

    // 2. Check status (ACTIVE or APPROVED required for login)
    const isActive = user.status === 'ACTIVE' || user.status === 'APPROVED';
    if (!isActive) {
        let errMessage = "Access not approved";
        if (user.status === 'PENDING') {
            errMessage = "Your access request is pending admin approval.";
        } else if (user.status === 'REJECTED') {
            errMessage = "Your access request was rejected. Please contact admin.";
        }
        const err = new Error(errMessage);
        err.status = 403;
        throw err;
    }

    // 3. Handle initial login with NULL password (approved but not yet set)
    if (user.password === null) {
        console.log(`[AUTH] User ${email} (APPROVED) has NULL password. Redirecting to Set Password.`);
        return res.status(200).json({
            message: "Credentials valid. Please set your password.",
            requiresPasswordSet: true,
            user: {
                id: user.id,
                name: user.name,
                email: user.email,
                role: user.role
            }
        });
    }

    // 4. Regular password validation
    const match = await authService.comparePassword(password, user.password);
    if (!match) {
        const err = new Error("Invalid password");
        err.status = 401;
        throw err;
    }

    if (!user.is_active) {
        const err = new Error("Account is inactive");
        err.status = 403;
        throw err;
    }

    const token = authService.generateToken({
        id: user.id,
        email: user.email,
        role: user.role
    });

    res.json({
        message: "Login successful",
        token: token,
        user: {
            id: user.id,
            name: user.name,
            email: user.email,
            role: user.role,
            phone: user.phone,
            department: user.department,
            professional_title: user.professional_title,
            avatar_url: user.avatar_url
        },
        settings: {
            isTwoFactorEnabled: user.isTwoFactorEnabled,
            isNotificationsEnabled: user.isNotificationsEnabled,
            isWhatsAppEnabled: user.isWhatsAppEnabled,
            isEmailEnabled: user.isEmailEnabled,
            isSecurityAlertsEnabled: user.isSecurityAlertsEnabled,
            isLatencyEnabled: user.isLatencyEnabled,
            isOnboardingEnabled: user.isOnboardingEnabled
        }
    });
});

exports.forgotPassword = asyncHandler(async (req, res) => {
    console.log(`[AUTH] Forgot Password requested for: ${req.body.email}`);
    const { email } = req.body;
    if (!email) {
        const err = new Error("Email is required");
        err.status = 400;
        throw err;
    }

    const result = await new Promise((resolve, reject) => {
        db.query("SELECT * FROM users WHERE email = ?", [email], (err, rows) => {
            if (err) return reject(err);
            resolve(rows);
        });
    });

    if (result.length === 0) {
        const err = new Error("User with this email does not exist");
        err.status = 404;
        throw err;
    }

    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    const otpExpiry = new Date(Date.now() + 5 * 60 * 1000);

    await new Promise((resolve, reject) => {
        db.query("UPDATE users SET otp = ?, otp_expiry = ? WHERE email = ?", [otp, otpExpiry, email], (err) => {
            if (err) return reject(err);
            resolve();
        });
    });

    const emailSent = await emailService.sendOtpEmail(email, otp);
    if (!emailSent) {
        return res.status(500).json({ message: "Failed to send OTP email. Please check server configuration." });
    }

    console.log(`[AUTH] OTP sent to ${email}`);
    res.json({ message: "OTP has been sent to your email address." });
});

exports.verifyOtp = asyncHandler(async (req, res) => {
    const { email, otp } = req.body;
    if (!email || !otp) {
        const err = new Error("Email and OTP are required");
        err.status = 400;
        throw err;
    }

    const result = await new Promise((resolve, reject) => {
        db.query("SELECT * FROM users WHERE email = ? AND otp = ? AND otp_expiry > NOW()", [email, otp], (err, rows) => {
            if (err) return reject(err);
            resolve(rows);
        });
    });

    if (result.length === 0) {
        const err = new Error("Invalid or expired OTP");
        err.status = 400;
        throw err;
    }

    res.json({ message: "OTP verified successfully." });
});

/**
 * REQUEST ACCESS — Email-based lookup only.
 * Admin must pre-create the user (status=CREATED) with an email.
 * User provides their email → validated → status becomes PENDING.
 * No auto-creation of new users allowed.
 */
exports.requestAccess = asyncHandler(async (req, res) => {
    let { email, role, department, pharmacy, professional_title, name, phone } = req.body;
    console.log('[DEBUG_REQUEST_ACCESS] Full Body:', JSON.stringify(req.body));

    if (!email || !role) {
        const err = new Error("Email and Role are required");
        err.status = 400;
        throw err;
    }

    // Server-side normalization
    name = normalizeName(name, role);

    if (role === 'patient') {
        const err = new Error("Access not allowed for patients. Please contact admin.");
        err.status = 403;
        throw err;
    }

    const existingUser = await new Promise((resolve, reject) => {
        db.query(
            "SELECT id, name, email, role, status FROM users WHERE email = ?",
            [email.toLowerCase().trim()],
            (err, rows) => {
                if (err) return reject(err);
                resolve(rows.length > 0 ? rows[0] : null);
            }
        );
    });

    if (!existingUser) {
        const err = new Error("This email is not registered. Please contact admin.");
        err.status = 404;
        throw err;
    }

    if (existingUser.role.toLowerCase() !== role.toLowerCase()) {
        const err = new Error("Selected role does not match registered role.");
        err.status = 403;
        throw err;
    }

    if (existingUser.status === 'ACTIVE') {
        const err = new Error("Account already active.");
        err.status = 409;
        throw err;
    }

    if (existingUser.status === 'PENDING' && existingUser.is_active === 0) {
        // Allow re-submitting request if not yet active
    } else if (existingUser.status === 'PENDING') {
        const err = new Error("Request already pending.");
        err.status = 409;
        throw err;
    }

    const finalDept = (role === 'pharmacist' ? pharmacy : department) || null;

    await withTransaction(async (connection) => {
        await connection.query(
            "UPDATE users SET status = 'PENDING', name = COALESCE(?, name), phone = COALESCE(?, phone), department = COALESCE(?, department), professional_title = COALESCE(?, professional_title) WHERE id = ?",
            [name, phone, finalDept, professional_title, existingUser.id]
        );

        if (role === 'doctor') {
            await connection.query(
                "UPDATE doctors SET name = COALESCE(?, name), phone = COALESCE(?, phone), specialization = COALESCE(?, specialization), professional_title = COALESCE(?, professional_title) WHERE user_id = ?",
                [name, phone, finalDept, professional_title, existingUser.id]
            );
        } else if (role === 'pharmacist') {
            await connection.query(
                "UPDATE pharmacists SET name = COALESCE(?, name), phone = COALESCE(?, phone) WHERE user_id = ?",
                [name, phone, existingUser.id]
            );
        }
    });

    res.status(200).json({ message: "Access request submitted. Please wait for approval." });
});

exports.resetPassword = asyncHandler(async (req, res) => {
    const { email, otp, newPassword } = req.body;
    if (!email || !otp || !newPassword) {
        const err = new Error("Email, OTP and new password are required");
        err.status = 400;
        throw err;
    }

    const result = await new Promise((resolve, reject) => {
        db.query("SELECT * FROM users WHERE email = ? AND otp = ? AND otp_expiry > NOW()", [email, otp], (err, rows) => {
            if (err) return reject(err);
            resolve(rows);
        });
    });

    if (result.length === 0) {
        const err = new Error("Invalid or expired OTP");
        err.status = 400;
        throw err;
    }

    const hashedPassword = await bcrypt.hash(newPassword, 10);

    await new Promise((resolve, reject) => {
        db.query("UPDATE users SET password = ?, otp = NULL, otp_expiry = NULL WHERE email = ?", [hashedPassword, email], (err) => {
            if (err) return reject(err);
            resolve();
        });
    });

    res.json({ message: "Password updated successfully." });
});

/**
 * SET PASSWORD — for first-time use by approved users.
 * POST /api/auth/set-password
 */
exports.setPassword = asyncHandler(async (req, res) => {
    const { email, password } = req.body;

    if (!email || !password) {
        const err = new Error("Email and password are required");
        err.status = 400;
        throw err;
    }

    const result = await new Promise((resolve, reject) => {
        db.query("SELECT * FROM users WHERE email = ?", [email.toLowerCase().trim()], (err, rows) => {
            if (err) return reject(err);
            resolve(rows);
        });
    });

    if (result.length === 0) {
        const err = new Error("User not found");
        err.status = 404;
        throw err;
    }

    const user = result[0];

    if (user.status !== 'ACTIVE') {
        const err = new Error("Account not approved. Cannot set password.");
        err.status = 403;
        throw err;
    }

    if (user.password !== null) {
        const err = new Error("Password already set. Please login or use Forgot Password.");
        err.status = 400;
        throw err;
    }

    const hashedPassword = await bcrypt.hash(password, 10);

    await withTransaction(async (connection) => {
        await connection.query("UPDATE users SET password = ? WHERE id = ?", [hashedPassword, user.id]);
        
        if (user.role === 'doctor') {
            await connection.query("UPDATE doctors SET password = ? WHERE user_id = ?", [hashedPassword, user.id]);
        } else if (user.role === 'pharmacist') {
            await connection.query("UPDATE pharmacists SET password = ? WHERE user_id = ?", [hashedPassword, user.id]);
        }
    });

    console.log(`[AUTH] User ${email} has set their first-time password.`);
    res.status(200).json({ message: "Password set successfully. You can now login." });
});

exports.changePassword = asyncHandler(async (req, res) => {
    const { currentPassword, newPassword } = req.body;
    const userId = req.user.id;

    if (!currentPassword || !newPassword) {
        const err = new Error("Current and new passwords are required");
        err.status = 400;
        throw err;
    }

    const result = await new Promise((resolve, reject) => {
        db.query("SELECT * FROM users WHERE id = ?", [userId], (err, rows) => {
            if (err) return reject(err);
            resolve(rows);
        });
    });

    if (result.length === 0) {
        const err = new Error("User not found");
        err.status = 404;
        throw err;
    }

    const user = result[0];
    const match = await authService.comparePassword(currentPassword, user.password);
    if (!match) {
        const err = new Error("Incorrect current password");
        err.status = 401;
        throw err;
    }

    const hashedPassword = await bcrypt.hash(newPassword, 10);
    await new Promise((resolve, reject) => {
        db.query("UPDATE users SET password = ? WHERE id = ?", [hashedPassword, userId], (err) => {
            if (err) return reject(err);
            resolve();
        });
    });

    res.json({ message: "Password updated successfully" });
});

/**
 * REGISTER — Admin creates staff/patients.
 * Email is MANDATORY for doctor/pharmacist/admin roles.
 * Patients still get auto-generated email if not provided.
 */
exports.register = asyncHandler(async (req, res) => {
    let { name, email, password, role, age, gender, phone, mobile, department, pharmacyUnit, professionalTitle, experience } = req.body;
    
    // Server-side normalization
    name = normalizeName(name, role);
    
    // mobile = laptop entry, phone = legacy. 
    const finalPhone = mobile || phone || '';
    // Clean experience: ensure it's stored as e.g. "5 Years" or just the number string
    const finalExperience = experience ? experience.toString().trim() : null;

    console.log('[REGISTER] Admin creating user. Payload:', { name, email, role, phone: finalPhone, department, pharmacyUnit, professionalTitle, experience: finalExperience });

    if (!name || !role || !finalPhone) {
        const err = new Error("Missing required fields: name, role, mobile/phone");
        err.status = 400;
        throw err;
    }
    
    const validRoles = ["admin", "doctor", "pharmacist", "patient"];
    if (!validRoles.includes(role)) {
        const err = new Error("Invalid role specified");
        err.status = 400;
        throw err;
    }

    if (role !== 'patient' && !email) {
        const err = new Error("Email is required for " + role + " registration");
        err.status = 400;
        throw err;
    }


    if (role !== 'patient') {
        if (!password || password.length < 6) {
            const err = new Error("Password is required and must be at least 6 characters");
            err.status = 400;
            throw err;
        }
    }

    const finalEmail = email ? email.toLowerCase().trim() : await generateEmail(name, role);

    // SERVER-SIDE VALIDATION
    const nameRegex = /^[a-zA-Z\s.]{2,50}$/;
    const phoneRegex = /^[0-9]{10}$/;
    const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/;

    if (!nameRegex.test(name)) {
        const err = new Error("Name must be 2-50 characters (letters, spaces, and dots only)");
        err.status = 400;
        throw err;
    }

    if (!phoneRegex.test(finalPhone)) {
        const err = new Error("Mobile number must be exactly 10 digits");
        err.status = 400;
        throw err;
    }

    if (!emailRegex.test(finalEmail)) {
        const err = new Error("Invalid email format");
        err.status = 400;
        throw err;
    }

    const existing = await new Promise((resolve, reject) => {
        db.query("SELECT id, email, phone FROM users WHERE email = ? OR phone = ?", [finalEmail, finalPhone], (err, rows) => {
            if (err) return reject(err);
            resolve(rows);
        });
    });

    if (existing.length > 0) {
        const isPhone = existing.some(u => u.phone === finalPhone);
        const err = new Error(isPhone ? "Mobile number already registered" : "Email already registered");
        err.status = 409;
        throw err;
    }

    const hashedPassword = role !== 'patient' ? await bcrypt.hash(password, 10) : null;

    const result = await withTransaction(async (connection) => {
        if (role === 'patient') {
            let assignedDoctorId = null;
            let finalDept = department || 'General Medicine';

            if (req.user && req.user.role === 'doctor') {
                const [doctorResult] = await connection.query("SELECT id FROM doctors WHERE user_id = ?", [req.user.id]);
                if (doctorResult.length > 0) assignedDoctorId = doctorResult[0].id;
            }

            // ONLY auto-assign if doctor exists for that department
            if (!assignedDoctorId) {
                const [deptDoctors] = await connection.query(
                    "SELECT id FROM doctors WHERE specialization = ? OR specialization LIKE ? LIMIT 1",
                    [finalDept, `%${finalDept}%`]
                );
                if (deptDoctors.length > 0) {
                    assignedDoctorId = deptDoctors[0].id;
                } else {
                    // STOP random assignments: 
                    // No doctor matches this department. Patient remains 'unassigned'.
                    assignedDoctorId = null; 
                }
            }

            const [userInsert] = await connection.query(
                "INSERT INTO users (name, email, password, role, is_active, status, phone, department, age, gender) VALUES (?, ?, ?, 'patient', 1, 'ACTIVE', ?, ?, ?, ?)",
                [name, finalEmail, null, finalPhone, finalDept, age || 0, gender || 'Not Specified']
            );
            const userId = userInsert.insertId;

            const [patientInsert] = await connection.query(
                "INSERT INTO patients (user_id, name, email, phone, doctor_id, department, age, gender) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                [userId, name, finalEmail, finalPhone, assignedDoctorId, finalDept, age || 0, gender || 'Not Specified']
            );
            const patientId = patientInsert.insertId;
            const formattedPid = `PID-${String(patientId).padStart(4, '0')}`;
            await connection.query("UPDATE patients SET pid = ? WHERE id = ?", [formattedPid, patientId]);

            return { userId, email: finalEmail, pid: formattedPid };

        } else {
            // DOCTORS / PHARMACISTS / ADMIN: status=CREATED, is_active=0, password=NULL
            const deptOrUnit = (role === 'pharmacist' ? pharmacyUnit : department) || null;
            const title = professionalTitle || (role === 'doctor' ? 'Junior Doctor' : null);

            const [userInsert] = await connection.query(
                "INSERT INTO users (name, email, password, role, is_active, status, phone, department, professional_title, age, gender, experience) VALUES (?, ?, ?, ?, 0, 'CREATED', ?, ?, ?, ?, ?, ?)",
                [name, finalEmail, hashedPassword, role, finalPhone, deptOrUnit, title, age || 0, gender || 'Not Specified', finalExperience]
            );
            const userId = userInsert.insertId;

            if (role === 'doctor') {
                await connection.query(
                    "INSERT INTO doctors (user_id, name, email, phone, specialization, professional_title, password) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    [userId, name, finalEmail, finalPhone, deptOrUnit || 'General Dentistry', title, hashedPassword]
                );
            } else if (role === 'pharmacist') {
                await connection.query(
                    "INSERT INTO pharmacists (user_id, name, email, phone, password) VALUES (?, ?, ?, ?, ?)",
                    [userId, name, finalEmail, finalPhone, hashedPassword]
                );
            }

            console.log('[REGISTER] Staff created via Admin. userId:', userId);
            return { userId, email: finalEmail };
        }
    });

    res.status(201).json({
        message: "User created successfully. They must use their email to request access.",
        userId: result.userId,
        email: result.email,
        pid: result.pid
    });
});

/**
 * Helper to generate name-based email (used for patients only now)
 */
async function generateEmail(name, role) {
    let base = name.toLowerCase()
        .replace(/^(dr|ph|mr|mrs|ms)(\.?)\s*/g, '')
        .replace(/\s+/g, '.')
        .replace(/[^a-z0-9.]/g, '');

    let emailBase = base;
    let email = `${emailBase}@diasrx.com`;
    let counter = 1;

    let exists = true;
    while (exists) {
        const result = await new Promise((resolve, reject) => {
            db.query("SELECT id FROM users WHERE email = ?", [email], (err, rows) => {
                if (err) return reject(err);
                resolve(rows.length > 0);
            });
        });

        if (!result) {
            exists = false;
        } else {
            email = `${emailBase}${counter}@diasrx.com`;
            counter++;
        }
    }
    return email;
}

/**
 * CHECK PRE-CREATED USER by email — used by RequestAccessActivity before showing the form.
 * GET /api/auth/check-user?email=...
 */
exports.checkPreCreatedUser = asyncHandler(async (req, res) => {
    const { email } = req.query;

    if (!email) {
        const err = new Error("Email query parameter is required");
        err.status = 400;
        throw err;
    }

    const users = await new Promise((resolve, reject) => {
        db.query(
            "SELECT id, name, email, role, status, phone, department, professional_title FROM users WHERE email = ?",
            [email.toLowerCase().trim()],
            (err, rows) => {
                if (err) return reject(err);
                resolve(rows);
            }
        );
    });

    if (users.length === 0) {
        const err = new Error("This email is not registered in the system. Please contact your admin.");
        err.status = 404;
        throw err;
    }

    const user = users[0];

    // Return fields for auto-fill in RequestAccessActivity
    res.status(200).json({
        found: true,
        id: user.id,
        name: user.name,
        role: user.role,
        status: user.status,
        email: user.email,
        phone: user.phone,
        department: user.department,
        professional_title: user.professional_title
    });
});
exports.getUserSettings = asyncHandler(async (req, res) => {
    const userId = req.user.id;
    const result = await new Promise((resolve, reject) => {
        db.query(
            `SELECT name, phone, email, avatar_url,
                    isTwoFactorEnabled, isNotificationsEnabled, isWhatsAppEnabled,
                    isEmailEnabled, isLatencyEnabled, isOnboardingEnabled, isSecurityAlertsEnabled
             FROM users WHERE id = ?`,
            [userId],
            (err, rows) => {
                if (err) return reject(err);
                resolve(rows);
            }
        );
    });

    if (result.length === 0) {
        return res.status(404).json({ message: "User not found" });
    }

    const row = result[0];
    res.json({
        user: {
            name: row.name,
            phone: row.phone,
            email: row.email,
            avatar_url: row.avatar_url
        },
        settings: {
            isTwoFactorEnabled: row.isTwoFactorEnabled,
            isNotificationsEnabled: row.isNotificationsEnabled,
            isWhatsAppEnabled: row.isWhatsAppEnabled,
            isEmailEnabled: row.isEmailEnabled,
            isSecurityAlertsEnabled: row.isSecurityAlertsEnabled,
            isLatencyEnabled: row.isLatencyEnabled,
            isOnboardingEnabled: row.isOnboardingEnabled
        }
    });
});

exports.updateUserSettings = asyncHandler(async (req, res) => {
    const userId = req.user.id;
    let { isTwoFactorEnabled, isNotificationsEnabled, isWhatsAppEnabled, isEmailEnabled, isSecurityAlertsEnabled, isLatencyEnabled, isOnboardingEnabled } = req.body;

    // Convert to boolean/tinyint correctly (OFF by default for safety)
    const toInt = (val) => (val === true || val === 'true' || val === 1 ? 1 : 0);
    
    const settings = {
        isTwoFactorEnabled: toInt(isTwoFactorEnabled),
        isNotificationsEnabled: toInt(isNotificationsEnabled),
        isWhatsAppEnabled: toInt(isWhatsAppEnabled),
        isEmailEnabled: toInt(isEmailEnabled),
        isSecurityAlertsEnabled: toInt(isSecurityAlertsEnabled),
        isLatencyEnabled: toInt(isLatencyEnabled),
        isOnboardingEnabled: toInt(isOnboardingEnabled)
    };

    await new Promise((resolve, reject) => {
        db.query(
            "UPDATE users SET isTwoFactorEnabled = ?, isNotificationsEnabled = ?, isWhatsAppEnabled = ?, isEmailEnabled = ?, isSecurityAlertsEnabled = ?, isLatencyEnabled = ?, isOnboardingEnabled = ? WHERE id = ?",
            [settings.isTwoFactorEnabled, settings.isNotificationsEnabled, settings.isWhatsAppEnabled, settings.isEmailEnabled, settings.isSecurityAlertsEnabled, settings.isLatencyEnabled, settings.isOnboardingEnabled, userId],
            (err) => {
                if (err) return reject(err);
                resolve();
            }
        );
    });

    res.json({
        message: "Settings updated successfully",
        isTwoFactorEnabled: isTwoFactorEnabled === 1,
        isNotificationsEnabled: isNotificationsEnabled === 1,
        isWhatsAppEnabled: isWhatsAppEnabled === 1,
        isEmailEnabled: isEmailEnabled === 1,
        isLatencyEnabled: isLatencyEnabled === 1,
        isOnboardingEnabled: isOnboardingEnabled === 1
    });
});

exports.updateProfile = asyncHandler(async (req, res) => {
    const userId = req.user.id;
    const { name, phone, professional_title, specialization, avatar_url } = req.body;
    const role = req.user.role;

    // 1. Update the base users table (Now includes avatar_url)
    const updateUsersSql = `
        UPDATE users 
        SET name = ?, phone = ?, professional_title = ?, department = ?, avatar_url = ? 
        WHERE id = ?
    `;
    
    await new Promise((resolve, reject) => {
        db.query(updateUsersSql, [name, phone, professional_title, specialization, avatar_url, userId], (err) => {
            if (err) return reject(err);
            resolve();
        });
    });

    // 2. Sync to role-specific tables
    if (role === 'doctor') {
        const updateDoctorSql = `
            UPDATE doctors 
            SET name = ?, phone = ?, professional_title = ?, specialization = ?, avatar_url = ? 
            WHERE user_id = ?
        `;
        await new Promise((resolve, reject) => {
            db.query(updateDoctorSql, [name, phone, professional_title, specialization, avatar_url, userId], (err) => {
                if (err) return reject(err);
                resolve();
            });
        });
    } else if (role === 'pharmacist') {
        const updatePharmacistSql = `
            UPDATE pharmacists 
            SET name = ?, phone = ?, professional_title = ?, department = ?, avatar_url = ? 
            WHERE user_id = ?
        `;
        await new Promise((resolve, reject) => {
            db.query(updatePharmacistSql, [name, phone, professional_title, specialization, avatar_url, userId], (err) => {
                if (err) return reject(err);
                resolve();
            });
        });
    }

    res.status(200).json({
        message: "Profile updated successfully",
        user: { name, phone, professional_title, specialization, avatar_url }
    });
});
