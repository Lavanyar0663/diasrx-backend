const asyncHandler = require('express-async-handler');
const db = require("../db");
const bcrypt = require("bcrypt");
const { withTransaction } = require("../utils/dbHelper");

exports.getPendingRequests = (req, res, next) => {
    const sql = `
        SELECT id, name, email, role, phone, department, created_at,
               DATE_FORMAT(created_at, '%d/%m/%Y | %H:%i') as formatted_date, status
        FROM users 
        WHERE status = 'PENDING' AND role IN ('doctor', 'pharmacist', 'patient', 'admin')
        ORDER BY created_at DESC
    `;
    db.query(sql, [], (err, results) => {
        if (err) return next(err);
        res.status(200).json(results);
    });
};

exports.getAllRequests = (req, res, next) => {
    const sql = `
        SELECT id, name, email, role, phone, department, created_at,
               DATE_FORMAT(created_at, '%d/%m/%Y | %H:%i') as formatted_date, status
        FROM users 
        WHERE role IN ('doctor', 'pharmacist', 'patient', 'admin')
        AND status IN ('PENDING', 'ACTIVE', 'REJECTED')
        ORDER BY updated_at DESC
    `;
    db.query(sql, [], (err, results) => {
        if (err) return next(err);
        res.status(200).json(results);
    });
};

exports.approveRequest = asyncHandler(async (req, res) => {
    const userId = req.params.id;
    let approvedEmail = '';

    await withTransaction(async (connection) => {
        const [users] = await connection.query("SELECT * FROM users WHERE id = ?", [userId]);
        if (users.length === 0) {
            const error = new Error("User request not found");
            error.status = 404;
            throw error;
        }
        const user = users[0];
        approvedEmail = user.email;

        // Update status and activate
        await connection.query(
            "UPDATE users SET status = 'ACTIVE', is_active = 1 WHERE id = ?",
            [userId]
        );

        // Sync to role-specific table with NULL password and active state
        if (user.role === 'doctor') {
            await connection.query(
                "UPDATE doctors SET is_active = 1 WHERE user_id = ?",
                [userId]
            );
        } else if (user.role === 'pharmacist') {
            await connection.query(
                "UPDATE pharmacists SET is_active = 1 WHERE user_id = ?",
                [userId]
            );
        } else if (user.role === 'patient') {
            await connection.query("UPDATE patients SET is_active = 1 WHERE user_id = ?", [userId]);
        }
    });

    console.log(`[ADMIN] Approved userId ${userId}. Password set to NULL for first-time login flow.`);

    res.status(200).json({
        message: "Request approved. User must now set their password on first login.",
        email: approvedEmail,
        requiresPasswordSet: true
    });
});

exports.rejectRequest = asyncHandler(async (req, res) => {
    const userId = req.params.id;

    // Fetch user to check role
    const users = await new Promise((resolve, reject) => {
        db.query("SELECT id, role FROM users WHERE id = ?", [userId], (err, rows) => {
            if (err) return reject(err);
            resolve(rows);
        });
    });

    if (users.length === 0) {
        const err = new Error("User request not found");
        err.status = 404;
        throw err;
    }

    // Always set to REJECTED so the request is removed from the pending queue
    await new Promise((resolve, reject) => {
        db.query(
            "UPDATE users SET status = 'REJECTED', is_active = 0 WHERE id = ?",
            [userId],
            (err, result) => {
                if (err) return reject(err);
                resolve(result);
            }
        );
    });

    console.log('[ADMIN] Rejected userId:', userId, '→ status set to: REJECTED');
    res.status(200).json({ message: "Request rejected successfully." });
});

/**
 * CHECK PRE-CREATED USER by email — used by RequestAccessActivity before showing the form.
 * GET /api/admin/check-user?email=...

/**
 * DELETE USER — fully removes from users + role-specific table
 * DELETE /api/admin/users/:id
 */
exports.deleteUser = asyncHandler(async (req, res) => {
    const userId = req.params.id;

    await withTransaction(async (connection) => {
        const [users] = await connection.query("SELECT * FROM users WHERE id = ?", [userId]);
        if (users.length === 0) {
            const error = new Error("User not found");
            error.status = 404;
            throw error;
        }
        const user = users[0];

        // Delete from role-specific table first (FK constraint)
        if (user.role === 'doctor') {
            await connection.query("DELETE FROM doctors WHERE user_id = ?", [userId]);
        } else if (user.role === 'pharmacist') {
            await connection.query("DELETE FROM pharmacists WHERE user_id = ?", [userId]);
        } else if (user.role === 'patient') {
            await connection.query("DELETE FROM patients WHERE user_id = ?", [userId]);
        }

        // Then delete from users
        await connection.query("DELETE FROM users WHERE id = ?", [userId]);
        console.log('[ADMIN] Deleted userId:', userId, 'role:', user.role);
    });

    res.status(200).json({ message: "User deleted successfully." });
});

exports.getAdminStats = async (req, res, next) => {
    try {
        const [doctorCount] = await new Promise((resolve, reject) => {
            db.query("SELECT COUNT(*) as count FROM doctors d JOIN users u ON d.user_id = u.id WHERE u.status = 'ACTIVE'", (err, res) => err ? reject(err) : resolve(res));
        });
        const [pharmCount] = await new Promise((resolve, reject) => {
            db.query("SELECT COUNT(*) as count FROM pharmacists p JOIN users u ON p.user_id = u.id WHERE u.status = 'ACTIVE'", (err, res) => err ? reject(err) : resolve(res));
        });
        const [patientCount] = await new Promise((resolve, reject) => {
            db.query("SELECT COUNT(*) as count FROM patients", (err, res) => err ? reject(err) : resolve(res));
        });
        const [pendingCount] = await new Promise((resolve, reject) => {
            db.query("SELECT COUNT(*) as count FROM users WHERE status = 'PENDING'", (err, res) => err ? reject(err) : resolve(res));
        });
        const [activeCount] = await new Promise((resolve, reject) => {
            db.query("SELECT COUNT(*) as count FROM users WHERE status = 'ACTIVE'", (err, res) => err ? reject(err) : resolve(res));
        });
        const [rejectedCount] = await new Promise((resolve, reject) => {
            db.query("SELECT COUNT(*) as count FROM users WHERE status = 'REJECTED'", (err, res) => err ? reject(err) : resolve(res));
        });

        res.status(200).json({
            doctors: doctorCount.count,
            pharmacists: pharmCount.count,
            patients: patientCount.count,
            pending: pendingCount.count,
            active: activeCount.count,
            rejected: rejectedCount.count
        });
    } catch (err) {
        next(err);
    }
};

exports.getDoctors = (req, res, next) => {
    const sql = `
        SELECT u.id as user_id, d.id as doctor_id, u.name, u.email, u.phone, u.department, u.professional_title, 
               d.specialization, COALESCE(u.experience, 'N/A') as experience, u.status, u.created_at, u.updated_at, u.age, u.gender,
               (SELECT COUNT(*) FROM patients p WHERE p.doctor_id = d.id) as patient_count
        FROM users u
        JOIN doctors d ON u.id = d.user_id
        WHERE u.role = 'doctor' AND u.status = 'ACTIVE' 
        ORDER BY u.name ASC
    `;
    db.query(sql, [], (err, results) => {
        if (err) return next(err);
        res.status(200).json(results);
    });
};

exports.getPharmacists = (req, res, next) => {
    const sql = `
        SELECT u.id, ph.id as pharmacist_id, u.name, u.email, u.phone, u.department, u.professional_title, 
               COALESCE(u.experience, 'N/A') as experience, u.status, u.created_at, u.updated_at, u.age, u.gender
        FROM users u
        LEFT JOIN pharmacists ph ON u.id = ph.user_id
        WHERE u.role = 'pharmacist' AND u.status = 'ACTIVE' 
        ORDER BY u.name ASC
    `;
    db.query(sql, [], (err, results) => {
        if (err) return next(err);
        res.status(200).json(results);
    });
};
