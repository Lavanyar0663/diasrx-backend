const asyncHandler = require("express-async-handler");
const db = require("../db");

exports.getPatients = (req, res, next) => {
    const role = req.user ? req.user.role : null;
    const userId = req.user ? req.user.id : null;

    if (role === 'doctor') {
        const sql = `
            SELECT p.*, d.user_id as assigned_doctor_user_id, d.name as doctor_name, DATE_FORMAT(u.created_at, '%d/%m/%Y, %H:%i') as formatted_date
            FROM patients p
            LEFT JOIN doctors d ON p.doctor_id = d.id
            LEFT JOIN users u ON p.user_id = u.id
            WHERE p.is_active = 1
            ORDER BY p.id DESC
        `;
        db.query(sql, [], (err, result) => {
            if (err) return next(err);
            res.json(result);
        });
    } else {
        db.query(`
            SELECT p.*, d.name as doctor_name, DATE_FORMAT(u.created_at, '%d/%m/%Y, %H:%i') as formatted_date
            FROM patients p 
            LEFT JOIN doctors d ON p.doctor_id = d.id 
            LEFT JOIN users u ON p.user_id = u.id
            WHERE p.is_active = 1 
            ORDER BY p.id DESC
        `, (err, result) => {
            if (err) return next(err);
            res.json(result);
        });
    }
};

exports.addPatient = asyncHandler(async (req, res, next) => {
    let { name, age, gender, phone, email, doctor_id, department } = req.body;

    if (!name || !phone) {
        const err = new Error("Missing required fields: name, phone");
        err.status = 400;
        return next(err);
    }

    // Auto-assign doctor if registrar is a doctor, else find one based on department
    let finalDoctorId = doctor_id;
    let finalDept = department || 'General Medicine';

    if (!finalDoctorId) {
        if (req.user && req.user.role === 'doctor') {
            const docResult = await new Promise((resolve, reject) => {
                db.query("SELECT id, specialization FROM doctors WHERE user_id = ?", [req.user.id], (err, rows) => err ? reject(err) : resolve(rows));
            });
            if (docResult.length > 0) {
                finalDoctorId = docResult[0].id;
                finalDept = docResult[0].specialization;
            }
        } else {
            // Find doctor by department or just any doctor
            const doctors = await new Promise((resolve, reject) => {
                db.query("SELECT id, specialization FROM doctors WHERE specialization = ? OR specialization LIKE ? LIMIT 1", [finalDept, `%${finalDept}%`], (err, rows) => err ? reject(err) : resolve(rows));
            });
            if (doctors.length > 0) {
                finalDoctorId = doctors[0].id;
                finalDept = doctors[0].specialization;
            } else {
                finalDoctorId = null;
            }
        }
    }

    if (!finalDoctorId) {
        const err = new Error("No available doctors found to assign to this patient.");
        err.status = 500;
        return next(err);
    }

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
