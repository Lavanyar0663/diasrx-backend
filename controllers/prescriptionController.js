const asyncHandler = require("express-async-handler");
const db = require("../db");
const prescriptionService = require("../services/prescriptionService");

exports.createFullPrescription = asyncHandler(async (req, res) => {
    const { patient_id, diagnosis, remarks, drugs } = req.body;

    // We must find the correct doctors.id rather than using users.id directly!
    const [docs] = await db.promise().query("SELECT id FROM doctors WHERE user_id = ?", [req.user.id]);
    if (docs.length === 0) {
        const err = new Error("Doctor profile not found for this user");
        err.status = 403;
        throw err;
    }
    const doctor_id = docs[0].id;

    if (!patient_id || !diagnosis || !drugs || !Array.isArray(drugs) || drugs.length === 0) {
        const err = new Error("Missing required fields: patient_id, diagnosis, or drugs (as non-empty array)");
        err.status = 400;
        throw err;
    }

    // Validate each drug
    for (const drug of drugs) {
        if (!drug.drug_id || !drug.duration || !drug.frequency) {
            const err = new Error(`Incomplete drug information for drug_id ${drug.drug_id || 'unknown'}. Duration and frequency are required.`);
            err.status = 400;
            throw err;
        }
    }

    const result = await prescriptionService.createPrescription({
        patient_id, doctor_id, diagnosis, remarks, drugs
    });

    res.status(201).json({
        message: "Prescription created successfully",
        prescription: result
    });
});

exports.dispensePrescription = asyncHandler(async (req, res) => {
    const { id } = req.params;
    const idempotencyKey = req.idempotencyKey; // From idempotencyMiddleware

    const result = await prescriptionService.dispensePrescription(id, idempotencyKey);
    res.status(200).json(result);
});

/* FETCH ENDPOINTS */
exports.getPrescriptionsByPatient = asyncHandler(async (req, res, next) => {
    const patientId = req.params.id;
    const { id: userId, role } = req.user;

    let sql = `
        SELECT p.*, d.name as doctor_name, d.specialization as doctor_department, DATE_FORMAT(p.created_at, '%d/%m/%Y, %H:%i') as formatted_date
        FROM prescription p
        JOIN doctors d ON p.doctor_id = d.id
        WHERE p.patient_id = ?
        ORDER BY p.created_at DESC`;
    let params = [patientId];

    const [results] = await db.promise().query(sql, params);

    // Attach nested drugs to each prescription
    const prescriptionsWithDrugs = await Promise.all(results.map(async (prescription) => {
        const drugsSql = `
            SELECT pd.*, dm.name as drug_name, dm.type as drug_type, dm.strength
            FROM prescription_drugs pd
            JOIN drug_master dm ON pd.drug_id = dm.id
            WHERE pd.prescription_id = ?
        `;
        const [drugResults] = await db.promise().query(drugsSql, [prescription.id]);
        prescription.drugs = drugResults;
        return prescription;
    }));

    res.status(200).json(prescriptionsWithDrugs);
});

exports.getPrescriptionsByDoctor = asyncHandler(async (req, res, next) => {
    const requestedId = req.params.id; // This is doctors.id
    const { id: userId, role } = req.user;

    let targetDoctorId = requestedId;

    if (role === "doctor") {
        const [docs] = await db.promise().query("SELECT id FROM doctors WHERE user_id = ?", [userId]);
        if (docs.length === 0) {
            return res.status(403).json({ message: "No doctor profile linked to this user" });
        }
        // Force the target to be their own doctor_id
        targetDoctorId = docs[0].id;
    }

    const sql = `
        SELECT p.*, pat.name as patient_name, pat.id as patient_display_id, 
               pat.gender as gender, pat.gender as pat_gender,
               DATE_FORMAT(p.created_at, '%Y-%m-%dT%H:%i:%s.000Z') as iso_created_at
        FROM prescription p
        JOIN patients pat ON p.patient_id = pat.id
        WHERE p.doctor_id = ?
        ORDER BY p.created_at DESC`;

    const [results] = await db.promise().query(sql, [targetDoctorId]);
    res.status(200).json(results);
});

exports.getPendingPrescriptions = asyncHandler(async (req, res, next) => {
    const sql = `
        SELECT p.*, d.name as doctor_name, d.specialization as doctor_department, 
               pat.name as patient_name, pat.id as patient_display_id, 
               pat.gender as gender, pat.gender as pat_gender,
               DATE_FORMAT(p.created_at, '%Y-%m-%dT%H:%i:%s.000Z') as iso_created_at
        FROM prescription p
        JOIN doctors d ON p.doctor_id = d.id
        JOIN patients pat ON p.patient_id = pat.id
        WHERE p.status = 'PENDING' OR p.status = 'CREATED' OR p.status = '' OR p.status IS NULL
        ORDER BY p.id DESC`;

    const [results] = await db.promise().query(sql);
    res.status(200).json(results);
});

exports.getPrescriptionHistory = asyncHandler(async (req, res, next) => {
    const sql = `
        SELECT p.*, d.name as doctor_name, d.specialization as doctor_department, 
               pat.name as patient_name, pat.id as patient_display_id, 
               pat.gender as gender, pat.gender as pat_gender,
               DATE_FORMAT(p.created_at, '%Y-%m-%dT%H:%i:%s.000Z') as iso_created_at
        FROM prescription p
        JOIN doctors d ON p.doctor_id = d.id
        JOIN patients pat ON p.patient_id = pat.id
        ORDER BY p.id DESC`;

    const [results] = await db.promise().query(sql);
    res.status(200).json(results);
});

exports.getPrescriptionById = (req, res, next) => {
    const { id } = req.params;
    console.log(`[DEBUG] getPrescriptionById called for id: ${id}`);

    const prescSql = `
        SELECT p.*, d.name as doctor_name, d.specialization as doctor_role, pat.name as patient_name, pat.id as patient_display_id, pat.age, pat.gender
        FROM prescription p
        JOIN doctors d ON p.doctor_id = d.id
        JOIN patients pat ON p.patient_id = pat.id
        WHERE p.id = ?`;

    db.query(prescSql, [id], (err, prescResults) => {
        if (err) {
            console.error(`[DEBUG] prescSql error: ${err.message}`);
            return next(err);
        }
        console.log(`[DEBUG] prescResults length: ${prescResults.length}`);
        if (prescResults.length === 0) {
            return res.status(404).json({ message: "Prescription not found" });
        }

        const prescription = prescResults[0];

        const drugsSql = `
            SELECT pd.*, dm.name as drug_name
            FROM prescription_drugs pd
            JOIN drug_master dm ON pd.drug_id = dm.id
            WHERE pd.prescription_id = ?`;

        db.query(drugsSql, [id], (err, drugResults) => {
            if (err) {
                return next(err);
            }
            prescription.drugs = drugResults;
            res.status(200).json(prescription);
        });
    });
};

exports.getPharmacistStats = asyncHandler(async (req, res) => {
    const statsSql = `
        SELECT 
            (SELECT COUNT(*) FROM prescription WHERE status IN ('PENDING', 'CREATED', '', NULL) OR status IS NULL) as totalPending,
            (SELECT COUNT(*) FROM prescription WHERE status = 'DISPENSED' AND DATE(updated_at) = CURDATE()) as dispensedToday,
            (
                (SELECT COUNT(*) FROM prescription WHERE status IN ('PENDING', 'CREATED', '', NULL) OR status IS NULL) + 
                (SELECT COUNT(*) FROM prescription WHERE status = 'DISPENSED' AND DATE(updated_at) = CURDATE())
            ) as totalToday,
            (SELECT COUNT(*) FROM prescription WHERE status = 'DISPENSED') as totalDispensed
    `;

    return new Promise((resolve, reject) => {
        db.query(statsSql, [], (err, results) => {
            if (err) {
                console.error("STATS QUERY FAILED:", err.message);
                return reject(err);
            }
            const data = results[0] || { totalPending: 0, dispensedToday: 0, totalToday: 0, totalDispensed: 0 };
            res.status(200).json(data);
            resolve();
        });
    });
});
