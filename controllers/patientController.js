const asyncHandler = require("express-async-handler");
const db = require("../db");

exports.getPatients = asyncHandler(async (req, res, next) => {
    const role = req.user ? req.user.role : null;
    const userId = req.user ? req.user.id : null;

    if (!userId) {
        console.error("[GET_PATIENTS_BULLETPROOF_ERROR] Missing user in request");
        return res.status(401).json({ message: "Authentication required" });
    }

    console.log(`[GET_PATIENTS_BULLETPROOF] Request by user.id: ${userId}, role: ${role}`);

    try {
        let sql = "";
        let params = [];

        if (role === 'doctor') {
            const [docs] = await db.promise().query("SELECT id, specialization FROM doctors WHERE user_id = ?", [userId]);
            if (docs.length === 0) {
                return res.status(200).json([]);
            }
            const docId = docs[0].id;
            const spec = docs[0].specialization;

            sql = `
                SELECT 
                    p.id, p.name, p.pid, p.email, p.age, p.gender as pat_gender, p.doctor_id, p.is_active, p.is_visited, p.phone, p.department, p.user_id,
                    d.name as doctor_name,
                    IFNULL(DATE_FORMAT(u.created_at, '%d %b %Y'), 'N/A') as formatted_date
                FROM patients p
                LEFT JOIN doctors d ON p.doctor_id = d.id
                LEFT JOIN users u ON p.user_id = u.id
                WHERE p.is_active = 1 
                AND (p.doctor_id = ? OR (p.doctor_id IS NULL AND p.department = ?))
                ORDER BY p.id DESC
            `;
            params = [docId, spec];
        } else {
            // Admin/Pharmacist path
            sql = `
                SELECT 
                    id, name, pid, email, age, gender as pat_gender, doctor_id, is_active, is_visited, phone, department, user_id,
                    (SELECT name FROM doctors WHERE id = patients.doctor_id) as doctor_name,
                    IFNULL(DATE_FORMAT((SELECT created_at FROM users WHERE id = patients.user_id), '%d %b %Y'), 'N/A') as formatted_date
                FROM patients 
                WHERE is_active = 1 
                ORDER BY id DESC
            `;
        }

        const [result] = await db.promise().query(sql, params);
        console.log(`[GET_PATIENTS_BULLETPROOF] Query successful. Found ${result.length} patients.`);
        res.status(200).json(result || []);

    } catch (err) {
        console.error("[GET_PATIENTS_BULLETPROOF_ERROR]:", err.message);
        res.status(500).json({ 
            message: "Internal server error fetching patients", 
            error: err.message,
            sql_debug: "Bulletproof query failed"
        });
    }
});

exports.getPatientById = asyncHandler(async (req, res, next) => {
    const patientId = req.params.id;
    try {
        const sql = `
            SELECT 
                p.id, p.name, p.pid, p.email, p.age, p.gender, p.doctor_id, p.is_active, p.is_visited, p.phone, p.department, p.user_id,
                d.name as doctor_name,
                IFNULL(DATE_FORMAT(u.created_at, '%d %b %Y'), 'N/A') as formatted_date
            FROM patients p
            LEFT JOIN doctors d ON p.doctor_id = d.id
            LEFT JOIN users u ON p.user_id = u.id
            WHERE p.id = ? AND p.is_active = 1
        `;
        const [result] = await db.promise().query(sql, [patientId]);
        
        if (result.length === 0) {
            return res.status(404).json({ message: "Patient not found" });
        }
        res.status(200).json(result[0]);
    } catch (err) {
        console.error("[GET_PATIENT_BY_ID_ERROR]:", err.message);
        res.status(500).json({ message: "Internal server error", error: err.message });
    }
});

exports.addPatient = asyncHandler(async (req, res, next) => {
    let { name, age, gender, phone, email, doctor_id, department } = req.body;

    if (!name || !phone) {
        const err = new Error("Missing required fields: name, phone");
        err.status = 400;
        return next(err);
    }

    // Auto-assign doctor if registrar is a doctor, else find one based on department
    let finalDoctorId = doctor_id;
    let finalDept = department || 'General Dentistry';

    // FOR DOCTORS: Always override with their own doctor_id
    if (req.user && req.user.role === 'doctor') {
        const docResult = await new Promise((resolve, reject) => {
            db.query("SELECT id, specialization FROM doctors WHERE user_id = ?", [req.user.id], (err, rows) => err ? reject(err) : resolve(rows));
        });
        if (docResult.length > 0) {
            finalDoctorId = docResult[0].id;
            finalDept = docResult[0].specialization;
        } else {
            const err = new Error("Doctor record not found for this user");
            err.status = 404;
            return next(err);
        }
    } else if (!finalDoctorId) {
        // FOR ADMIN/OTHERS: Auto-assign based on department if not provided
        const doctors = await new Promise((resolve, reject) => {
            db.query("SELECT id, specialization FROM doctors WHERE specialization = ? OR specialization LIKE ? LIMIT 1", [finalDept, `%${finalDept}%`], (err, rows) => err ? reject(err) : resolve(rows));
        });
        if (doctors.length > 0) {
            finalDoctorId = doctors[0].id;
            finalDept = doctors[0].specialization;
        }
    }

    console.log(`[ADD_PATIENT] Assigning patient to doctorId: ${finalDoctorId}, Dept: ${finalDept}`);

    // EXPLANATION: doctor_id is now optional. If NULL, patient belongs to department pool.

    const sql =
        "INSERT INTO patients (name, age, gender, phone, email, doctor_id, department) VALUES (?, ?, ?, ?, ?, ?, ?)";

    db.query(sql, [name, age || 0, gender || 'Not Specified', phone, email || null, finalDoctorId, finalDept], (err, result) => {
        if (err) {
            if (err.code === "ER_DUP_ENTRY") return next(new Error("Patient with this phone number already exists"));
            return next(err);
        }
        
        const newId = result.insertId;
        const formattedPid = `PID-${String(newId).padStart(4, '0')}`;
        
        // Update the patient record with the generated PID
        db.query("UPDATE patients SET pid = ? WHERE id = ?", [formattedPid, newId], (updErr) => {
            if (updErr) console.error("Error updating PID:", updErr);
            res.status(201).json({ 
                message: "Patient added successfully", 
                id: newId,
                pid: formattedPid
            });
        });
    });
});

exports.assignDoctor = (req, res, next) => {
    const patientId = req.params.id;
    const { doctor_id } = req.body;

    if (!doctor_id) {
        const err = new Error("Missing doctor_id in request body");
        err.status = 400;
        return next(err);
    }

    db.query("SELECT id FROM doctors WHERE id = ?", [doctor_id], (err, results) => {
        if (err) return next(err);

        if (results.length === 0) {
            const notFound = new Error("Doctor not found");
            notFound.status = 404;
            return next(notFound);
        }

        const sql = "UPDATE patients SET doctor_id = ? WHERE id = ? AND is_active = 1";
        db.query(sql, [doctor_id, patientId], (assignErr, updateResult) => {
            if (assignErr) return next(assignErr);

            if (updateResult.affectedRows === 0) {
                const notFound = new Error("Patient not found");
                notFound.status = 404;
                return next(notFound);
            }

            res.status(200).json({ message: "Doctor assigned to patient successfully" });
        });
    });
};
// Update: New endpoint for dashboard info (Doctor)
exports.getDoctorDashboardInfo = asyncHandler(async (req, res) => {
    const doctorId = req.user.id;
    
    // 1. Get Doctor Basic Info (Name, Specialization)
    const [docs] = await db.promise().query(
        "SELECT name, specialization FROM doctors WHERE user_id = ? LIMIT 1",
        [doctorId]
    );
    
    if (docs.length === 0) {
        return res.status(404).json({ message: "Doctor profile not found" });
    }
    
    const doc = docs[0];

    // 2. Get today's OPD Load (Unique Patients with Pending prescriptions created today)
    const todayStr = new Date().toISOString().split('T')[0];
    const [load] = await db.promise().query(
        `SELECT COUNT(DISTINCT patient_id) as count FROM prescription 
         WHERE status IN ('PENDING', 'CREATED', 'ISSUED') 
         AND DATE(created_at) = ?`,
        [todayStr]
    );

    res.json({
        doctorName: doc.name,
        department: doc.specialization,
        opdLoad: load[0].count || 0
    });
});

// Update: New endpoint for doctor stats (Dashboard counts)
exports.getDoctorStats = asyncHandler(async (req, res) => {
    const userId = req.user.id;
    
    // 1. Get total patients visible to this doctor
    const [doc] = await db.promise().query("SELECT id, specialization FROM doctors WHERE user_id = ? LIMIT 1", [userId]);
    if (doc.length === 0) return res.status(404).json({ message: "Doctor not found" });
    
    const docId = doc[0].id;
    const spec = doc[0].specialization;

    const [patients] = await db.promise().query(
        `SELECT COUNT(*) as count FROM patients 
         WHERE is_active = 1 
         AND (doctor_id = ? OR (doctor_id IS NULL AND department = ?))`,
        [docId, spec]
    );

    // 2. Get pending and dispensed prescriptions
    const [prescStats] = await db.promise().query(
        `SELECT 
            SUM(CASE WHEN LOWER(status) IN ('pending', 'created', 'issued') THEN 1 ELSE 0 END) as pending,
            SUM(CASE WHEN LOWER(status) = 'dispensed' THEN 1 ELSE 0 END) as dispensed
         FROM prescription WHERE doctor_id = ?`,
        [docId]
    );

    res.json({
        totalPatients: patients[0].count || 0,
        pendingPrescriptions: prescStats[0].pending || 0,
        dispensedPrescriptions: prescStats[0].dispensed || 0
    });
});
