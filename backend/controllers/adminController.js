const asyncHandler = require('express-async-handler'); const db = require("../db");
const { withTransaction } = require("../utils/dbHelper");

exports.getPendingRequests = (req, res, next) => {
    // Return unique users with PENDING status by selecting the latest entry for each distinct name.
    const sql = `
        SELECT id, name, email, role, phone, department, created_at, DATE_FORMAT(created_at, '%d/%m/%Y | %H:%i') as formatted_date, status
        FROM users 
        WHERE id IN (
            SELECT MAX(id) FROM users WHERE status = 'PENDING' AND role IN ('doctor', 'pharmacist', 'patient', 'admin') GROUP BY phone
        )
        ORDER BY created_at DESC
    `;

    db.query(sql, [], (err, results) => {
        if (err) return next(err);
        res.status(200).json(results);
    });
};

exports.getAllRequests = (req, res, next) => {
    // Return latest unique staff users for admin access management (Pending, Approved, Rejected)
    // Group by name to address the specific issue of multiple test accounts with same name (e.g. Lavanya R)
    const sql = `
        SELECT id, name, email, role, phone, department, created_at, DATE_FORMAT(created_at, '%d/%m/%Y | %H:%i') as formatted_date, status
        FROM users 
        WHERE id IN (
            SELECT MAX(id) FROM users WHERE role IN ('doctor', 'pharmacist', 'patient', 'admin') GROUP BY phone
        )
        ORDER BY updated_at DESC
    `;

    db.query(sql, [], (err, results) => {
        if (err) return next(err);
        res.status(200).json(results);
    });
};

exports.approveRequest = asyncHandler(async (req, res, next) => {
    const userId = req.params.id;

    await withTransaction(async (connection) => {
        // 1. Fetch user details
        const [users] = await connection.query("SELECT * FROM users WHERE id = ?", [userId]);
        if (users.length === 0) {
            const error = new Error("User request not found");
            error.status = 404;
            throw error;
        }
        const user = users[0];

        // 2. Update status to APPROVED and activate
        await connection.query("UPDATE users SET status = 'APPROVED', is_active = 1 WHERE id = ?", [userId]);

        // 3. For patients, ensure role-specific table also has is_active = 1
        if (user.role === 'patient') {
            await connection.query("UPDATE patients SET is_active = 1 WHERE user_id = ?", [userId]);
        }
    });

    res.status(200).json({ message: "Request approved successfully." });
});

exports.rejectRequest = (req, res, next) => {
    const userId = req.params.id;

    // Update user status to REJECTED and ensure is_active stays 0
    const sql = "UPDATE users SET status = 'REJECTED', is_active = 0 WHERE id = ?";

    db.query(sql, [userId], (err, result) => {
        if (err) return next(err);

        if (result.affectedRows === 0) {
            const notFound = new Error("User request not found");
            notFound.status = 404;
            return next(notFound);
        }
        res.status(200).json({ message: "Request rejected successfully." });
    });
};

exports.getAdminStats = async (req, res, next) => {
    try {
        const [doctorCount] = await new Promise((resolve, reject) => {
            db.query("SELECT COUNT(*) as count FROM doctors", (err, res) => err ? reject(err) : resolve(res));
        });
        const [pharmCount] = await new Promise((resolve, reject) => {
            db.query("SELECT COUNT(*) as count FROM pharmacists", (err, res) => err ? reject(err) : resolve(res));
        });
        const [patientCount] = await new Promise((resolve, reject) => {
            db.query("SELECT COUNT(*) as count FROM patients", (err, res) => err ? reject(err) : resolve(res));
        });
        const [pendingCount] = await new Promise((resolve, reject) => {
            db.query("SELECT COUNT(*) as count FROM users WHERE status = 'PENDING'", (err, res) => err ? reject(err) : resolve(res));
        });
        const [approvedCount] = await new Promise((resolve, reject) => {
            db.query("SELECT COUNT(*) as count FROM users WHERE status = 'APPROVED'", (err, res) => err ? reject(err) : resolve(res));
        });
        const [rejectedCount] = await new Promise((resolve, reject) => {
            db.query("SELECT COUNT(*) as count FROM users WHERE status = 'REJECTED'", (err, res) => err ? reject(err) : resolve(res));
        });

        res.status(200).json({
            doctors: doctorCount.count,
            pharmacists: pharmCount.count,
            patients: patientCount.count,
            pending: pendingCount.count,
            approved: approvedCount.count,
            rejected: rejectedCount.count
        });
    } catch (err) {
        next(err);
    }
};

exports.getDoctors = (req, res, next) => {
    db.query("SELECT * FROM doctors ORDER BY name ASC", [], (err, results) => {
        if (err) return next(err);
        res.status(200).json(results);
    });
};

exports.getPharmacists = (req, res, next) => {
    db.query("SELECT * FROM pharmacists ORDER BY name ASC", [], (err, results) => {
        if (err) return next(err);
        res.status(200).json(results);
    });
};
