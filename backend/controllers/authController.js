const asyncHandler = require("express-async-handler");
const db = require("../db");
const authService = require("../services/authService");

exports.login = asyncHandler(async (req, res) => {
    const { email, password, role } = req.body;

    if (!email || !password || !role) {
        const err = new Error("Email, password and role are required");
        err.status = 400;
        throw err;
    }

    // Query from the unified users table
    const result = await new Promise((resolve, reject) => {
        db.query(`SELECT * FROM users WHERE email=? AND role=?`, [email, role.toLowerCase()], (err, rows) => {
            if (err) return reject(err);
            resolve(rows);
        });
    });

    if (result.length === 0) {
        const err = new Error("User not found or role mismatch");
        err.status = 404;
        throw err;
    }

    const user = result[0];

    // Check if user is active
    if (user.is_active === 0) {
        let errMessage = "Account is inactive. Please contact administrator.";
        if (user.status === 'CREATED') {
            errMessage = "Your account has been created by the admin. Please use the 'Join DIAS Rx' option on the login screen to request access using your mobile number.";
        } else if (user.status === 'PENDING') {
            errMessage = "Your account is pending admin approval. You will receive an SMS once approved.";
        }
        
        const err = new Error(errMessage);
        err.status = 403;
        throw err;
    }

    const match = await authService.comparePassword(password, user.password);
    if (!match) {
        const err = new Error("Invalid credentials");
        err.status = 401;
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
            role: user.role
        }
    });
});

exports.forgotPassword = asyncHandler(async (req, res) => {
    console.log(`[AUTH] Forgot Password requested for: ${req.body.email}`);
    const { email } = req.body;
    if (!email) {
        console.log("[AUTH] Error: Email is missing from request body");
        const err = new Error("Email is required");
        err.status = 400;
        throw err;
    }

    const result = await new Promise((resolve, reject) => {
        db.query("SELECT * FROM users WHERE email = ?", [email], (err, rows) => {
            if (err) {
                console.error(`[AUTH] DB Query Error: ${err.message}`);
                return reject(err);
            }
            resolve(rows);
        });
    });

    if (result.length === 0) {
        console.log(`[AUTH] Error: User not found for email ${email}`);
        const err = new Error("User with this email does not exist");
        err.status = 404;
        throw err;
    }

    // Generate 6-digit OTP
    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    const otpExpiry = new Date(Date.now() + 10 * 60 * 1000); // 10 minutes expiry

    await new Promise((resolve, reject) => {
        db.query("UPDATE users SET otp = ?, otp_expiry = ? WHERE email = ?", [otp, otpExpiry, email], (err) => {
            if (err) return reject(err);
            resolve();
        });
    });

    // In a real app, send actual email. For now, we log it.
    console.log(`OTP for ${email}: ${otp}`);
    res.json({ message: "OTP has been sent to your email.", otp: otp }); // Sending OTP in response for easier testing by user
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

exports.requestAccess = asyncHandler(async (req, res) => {
    const { phone, password, role, department, pharmacy, professional_title, name } = req.body;

    console.log('[REQUEST_ACCESS] Phone-based upgrade attempt:', { phone, role, name });

    if (!phone || !password) {
        const err = new Error("Phone and password are required");
        err.status = 400;
        throw err;
    }

    // Look up existing user by phone number
    const existingUser = await new Promise((resolve, reject) => {
        db.query("SELECT id, name, email, role, status, is_active FROM users WHERE phone = ?", [phone], (err, rows) => {
            if (err) return reject(err);
            resolve(rows.length > 0 ? rows[0] : null);
        });
    });

    if (!existingUser) {
        // Create an entirely new user with PENDING status!
        const finalEmail = await generateEmail(name || 'Unknown', role);
        const hashedPassword = await bcrypt.hash(password, 10);
        const finalDept = department || pharmacy || 'General Medicine';

        const insertId = await withTransaction(async (connection) => {
            const [usersResult] = await connection.query(
                "INSERT INTO users (name, email, phone, password, role, is_active, status, department, professional_title, age, gender) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                [name || 'Unknown', finalEmail, phone, hashedPassword, role, 0, 'PENDING', finalDept, professional_title || (role === 'doctor' ? 'Junior Doctor' : null), 0, 'Not Specified']
            );
            const userId = usersResult.insertId;

            if (role === 'doctor') {
                await connection.query(
                    "INSERT INTO doctors (user_id, name, email, phone, specialization, professional_title, password) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    [userId, name || 'Unknown', finalEmail, phone, finalDept, professional_title || 'Junior Doctor', hashedPassword]
                );
            } else if (role === 'pharmacist') {
                await connection.query(
                    "INSERT INTO pharmacists (user_id, name, email, phone, password) VALUES (?, ?, ?, ?, ?)",
                    [userId, name || 'Unknown', finalEmail, phone, hashedPassword]
                );
            }
            return userId;
        });

        console.log('[REQUEST_ACCESS] Created completely new PENDING user userId', insertId);
        return res.status(201).json({ 
            message: "Access request submitted successfully. Please wait for admin approval.", 
            userId: insertId 
        });
    }

    const { status } = existingUser;

    if (status === 'PENDING') {
        const err = new Error("Request already pending. Please wait for admin approval.");
        err.status = 409;
        throw err;
    }

    if (status === 'APPROVED') {
        const err = new Error(`Account already exists, please login. Your login email is: ${existingUser.email}`);
        err.status = 409;
        throw err;
    }

    if (status === 'REJECTED') {
        const err = new Error("Your previous access request was rejected. Please contact admin.");
        err.status = 403;
        throw err;
    }

    // Status is CREATED -> upgrade to PENDING and update password
    const hashedPassword = await bcrypt.hash(password, 10);
    const finalDept = department || pharmacy || null;
    const finalTitle = professional_title || null;

    await withTransaction(async (connection) => {
        // Update user status and name + store new password
        await connection.query(
            "UPDATE users SET status = 'PENDING', name = ?, password = ?, department = COALESCE(?, department), professional_title = COALESCE(?, professional_title) WHERE id = ?",
            [name || existingUser.name, hashedPassword, finalDept, finalTitle, existingUser.id]
        );

        // Update role-specific record too
        if (existingUser.role === 'doctor') {
            await connection.query(
                "UPDATE doctors SET name = ?, password = ?, specialization = COALESCE(?, specialization), professional_title = COALESCE(?, professional_title) WHERE user_id = ?",
                [name || existingUser.name, hashedPassword, finalDept, finalTitle, existingUser.id]
            );
        } else if (existingUser.role === 'pharmacist') {
            await connection.query(
                "UPDATE pharmacists SET name = ?, password = ? WHERE user_id = ?",
                [name || existingUser.name, hashedPassword, existingUser.id]
            );
        }
    });

    console.log('[REQUEST_ACCESS] Upgraded userId', existingUser.id, 'from CREATED to PENDING');
    res.status(200).json({ 
        message: "Access request submitted successfully. Please wait for admin approval.",
        userId: existingUser.id
    });
});


exports.resetPassword = asyncHandler(async (req, res) => {
    const { email, otp, newPassword } = req.body;
    if (!email || !otp || !newPassword) {
        const err = new Error("Email, OTP and new password are required");
        err.status = 400;
        throw err;
    }

    // Verify OTP again to be safe
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

const bcrypt = require("bcrypt");
const { withTransaction } = require("../utils/dbHelper");

exports.register = asyncHandler(async (req, res) => {
    let { name, email, password, role, age, gender, phone, department } = req.body;

    console.log('[REGISTER] Admin creating user. Payload:', { name, email, role, phone, department, age, gender });

    if (!name || !role || !phone) {
        const err = new Error("Missing required fields: name, role, phone");
        err.status = 400;
        throw err;
    }

    const validRoles = ["admin", "doctor", "pharmacist", "patient"];
    if (!validRoles.includes(role)) {
        const err = new Error("Invalid role specified");
        err.status = 400;
        throw err;
    }

    // Generate email if not provided
    const finalEmail = email || await generateEmail(name, role);

    // PRE-CHECK: UNIQUE PHONE & EMAIL
    const existing = await new Promise((resolve, reject) => {
        db.query("SELECT id, email, phone FROM users WHERE email = ? OR phone = ?", [finalEmail, phone], (err, rows) => {
            if (err) return reject(err);
            resolve(rows);
        });
    });

    if (existing.length > 0) {
        const isPhone = existing.some(u => u.phone === phone);
        const err = new Error(isPhone ? "Mobile number already registered" : "Email already registered");
        err.status = 409;
        throw err;
    }

    const hashedPassword = await bcrypt.hash(password || 'password123', 10);

        const result = await withTransaction(async (connection) => {
            if (role === 'patient') {
                // PATIENTS: created as APPROVED + is_active=1 immediately — no login needed
                let assignedDoctorId = 1;
                let finalDept = department || 'General Medicine';

                const [doctors] = await connection.query(
                    "SELECT id, specialization FROM doctors WHERE specialization LIKE ? LIMIT 1",
                    [`%${finalDept}%`]
                );
                if (doctors.length > 0) {
                    assignedDoctorId = doctors[0].id;
                } else {
                    assignedDoctorId = null;
                }

                const [usersResult] = await connection.query(
                    "INSERT INTO users (name, email, password, role, is_active, status, phone, department, age, gender) VALUES (?, ?, ?, 'patient', 1, 'APPROVED', ?, ?, ?, ?)",
                    [name, finalEmail, hashedPassword, phone, finalDept, age || 0, gender || 'Not Specified']
                );
                const userId = usersResult.insertId;

                const [patientResult] = await connection.query(
                    "INSERT INTO patients (user_id, name, email, phone, doctor_id, department, age, gender) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    [userId, name, finalEmail, phone, assignedDoctorId, finalDept, age || 0, gender || 'Not Specified']
                );
                const patientId = patientResult.insertId;
                const formattedPid = `PID-${String(patientId).padStart(4, '0')}`;
                await connection.query("UPDATE patients SET pid = ? WHERE id = ?", [formattedPid, patientId]);

                return { userId, email: finalEmail, pid: formattedPid };

            } else {
                // DOCTORS / PHARMACISTS / ADMIN: status=CREATED, is_active=0
                // They must request access with this phone to become PENDING then get approved
                const finalDept = department || (role === 'doctor' ? 'General Medicine' : null);
                const [usersResult] = await connection.query(
                    "INSERT INTO users (name, email, password, role, is_active, status, phone, department, professional_title, age, gender) VALUES (?, ?, ?, ?, 0, 'CREATED', ?, ?, ?, ?, ?)",
                    [name, finalEmail, hashedPassword, role, phone, finalDept, (role === 'doctor' ? 'Junior Doctor' : null), age || 0, gender || 'Not Specified']
                );
                const userId = usersResult.insertId;

                // Also insert into role-specific table immediately
                if (role === 'doctor') {
                    await connection.query(
                        "INSERT INTO doctors (user_id, name, email, phone, specialization, professional_title, password) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        [userId, name, finalEmail, phone, finalDept || 'General Medicine', 'Junior Doctor', hashedPassword]
                    );
                } else if (role === 'pharmacist') {
                    await connection.query(
                        "INSERT INTO pharmacists (user_id, name, email, phone, password) VALUES (?, ?, ?, ?, ?)",
                        [userId, name, finalEmail, phone, hashedPassword]
                    );
                }

                console.log('[REGISTER] Staff created with CREATED status. userId:', userId, 'role:', role);
                return { userId, email: finalEmail };
            }
        });

        res.status(201).json({ 
            message: "User created. They must now request access with this mobile number to get admin approval.",
            userId: result.userId, 
            email: result.email,
            pid: result.pid
        });
});
/**
 * Helper to sync user data to role-specific tables
 */
async function syncRoleData(userId, role, data) {
    const { name, email, phone, password, specialization, professional_title, age, gender, department } = data;
    const table = role === 'doctor' ? 'doctors' : (role === 'pharmacist' ? 'pharmacists' : (role === 'patient' ? 'patients' : null));

    if (!table) return;

    try {
        // Check if record exists in role table
        const existing = await new Promise((resolve, reject) => {
            db.query(`SELECT id FROM ${table} WHERE user_id = ? OR email = ?`, [userId, email], (err, rows) => {
                if (err) return reject(err);
                resolve(rows);
            });
        });

        if (existing.length > 0) {
            // UPDATE
            let query = "";
            let params = [];
            if (role === 'doctor') {
                query = "UPDATE doctors SET name=?, email=?, phone=?, password=?, specialization=?, professional_title=? WHERE id=?";
                params = [name, email, phone || null, password || null, specialization || department || 'General Dentistry', professional_title || 'Senior Doctor', existing[0].id];
            } else if (role === 'pharmacist') {
                query = "UPDATE pharmacists SET name=?, email=?, phone=?, password=? WHERE id=?";
                params = [name, email, phone || null, password || null, existing[0].id];
            } else if (role === 'patient') {
                query = "UPDATE patients SET name=?, email=?, phone=?, age=?, gender=?, department=? WHERE id=?";
                params = [name, email, phone || null, age || 30, gender || 'Unknown', specialization || department || 'General Medicine', existing[0].id];
            }

            if (query) {
                await new Promise((resolve, reject) => {
                    db.query(query, params, (err) => {
                        if (err) return reject(err);
                        resolve();
                    });
                });
            }
        } else {
            // INSERT
            let query = "";
            let params = [];
            if (role === 'doctor') {
                query = "INSERT INTO doctors (user_id, name, email, phone, password, specialization, professional_title) VALUES (?, ?, ?, ?, ?, ?, ?)";
                params = [userId, name, email, phone || null, password || null, specialization || department || 'General Dentistry', professional_title || 'Senior Doctor'];
            } else if (role === 'pharmacist') {
                query = "INSERT INTO pharmacists (user_id, name, email, phone, password) VALUES (?, ?, ?, ?, ?)";
                params = [userId, name, email, phone || null, password || null];
            } else if (role === 'patient') {
                query = "INSERT INTO patients (user_id, name, email, age, gender, phone, department, doctor_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                if (!phone) throw new Error('Phone is required for patient record');
                params = [userId, name, email, age || 30, gender || 'Unknown', phone, specialization || department || 'General Medicine', 1];
            }

            if (query) {
                await new Promise((resolve, reject) => {
                    db.query(query, params, (err) => {
                        if (err) return reject(err);
                        resolve();
                    });
                });
            }
        }
    } catch (err) {
        console.error(`Sync Error for ${role} (User ${userId}):`, err.message);
        throw err; // Re-throw to ensure the caller (e.g. requestAccess) knows it failed
    }
}
/**
 * Helper to generate name-based email
 */
async function generateEmail(name, role) {
    let base = name.toLowerCase()
        .replace(/^(dr|ph|mr|mrs|ms)(\.?)\s*/g, '') // Remove prefixes like Dr. smoothly without requiring space
        .replace(/\s+/g, '.') // Replace spaces with dots
        .replace(/[^a-z0-9.]/g, ''); // Remove special chars

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
