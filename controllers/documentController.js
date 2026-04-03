const asyncHandler = require("express-async-handler");
const path = require("path");
const db = require("../db");
const aiExplanationService = require("../services/aiExplanationService");
const pdfGeneratorService = require("../services/pdfGeneratorService");

/* ══════════════════════════════════════════════════════════════
   HELPER: checks if the requesting user is authorized to access
   a given prescription (enforced per role).
════════════════════════════════════════════════════════════════ */
const isAuthorized = (prescription, user) => {
    if (!prescription) return false;
    if (user.role === "admin") return true;
    if (user.role === "doctor") return String(prescription.doctor_id) === String(user.id);
    if (user.role === "patient") return String(prescription.patient_id) === String(user.id);
    return false;
};

/* ══════════════════════════════════════════════════════════════
   HELPER: fetches a full prescription with its drugs (drug_name joined)
════════════════════════════════════════════════════════════════ */
const fetchFullPrescription = (prescriptionId) => {
    return new Promise((resolve, reject) => {
        const sql = `
      SELECT p.*, 
             pt.name AS patient_name,
             d.name  AS doctor_name
      FROM prescription p
      LEFT JOIN patients pt ON pt.id = p.patient_id
      LEFT JOIN doctors  d  ON d.id  = p.doctor_id
      WHERE p.id = ?
    `;

        db.query(sql, [prescriptionId], (err, prescRows) => {
            if (err) return reject(err);
            if (prescRows.length === 0) return resolve({ prescription: null, drugs: [] });

            const prescription = prescRows[0];

            const drugSql = `
        SELECT pd.quantity, pd.frequency, dm.drug_name
        FROM prescription_drugs pd
        JOIN drug_master dm ON dm.id = pd.drug_id
        WHERE pd.prescription_id = ?
      `;

            db.query(drugSql, [prescriptionId], (drugErr, drugRows) => {
                if (drugErr) return reject(drugErr);
                resolve({ prescription, drugs: drugRows });
            });
        });
    });
};

/* ══════════════════════════════════════════════════════════════
   ENDPOINT 1: GET /api/prescriptions/:id/explained
════════════════════════════════════════════════════════════════ */
exports.getExplanation = asyncHandler(async (req, res) => {
    const { id } = req.params;
    const { prescription, drugs } = await fetchFullPrescription(id);

    if (!prescription) {
        const err = new Error("Prescription not found");
        err.status = 404;
        throw err;
    }

    if (!isAuthorized(prescription, req.user)) {
        const err = new Error("Forbidden: You are not authorized to view this prescription");
        err.status = 403;
        throw err;
    }

    const explanation = aiExplanationService.buildExplanation(prescription, drugs);
    res.status(200).json(explanation);
});

/* ══════════════════════════════════════════════════════════════
   ENDPOINT 2: POST /api/documents/prescription/:id/generate
════════════════════════════════════════════════════════════════ */
exports.generatePDF = asyncHandler(async (req, res) => {
    const { id } = req.params;
    const { prescription, drugs } = await fetchFullPrescription(id);

    if (!prescription) {
        const err = new Error("Prescription not found");
        err.status = 404;
        throw err;
    }

    if (!isAuthorized(prescription, req.user)) {
        const err = new Error("Forbidden: You cannot generate a PDF for this prescription");
        err.status = 403;
        throw err;
    }

    const explanation = aiExplanationService.buildExplanation(prescription, drugs);
    const pdfRelPath = await pdfGeneratorService.generatePrescriptionPDF(prescription, drugs, explanation);

    // Persist pdf_url to DB (fire and forget — non-blocking)
    db.query("UPDATE prescription SET pdf_url = ? WHERE id = ?", [pdfRelPath, id], (err) => {
        if (err) console.error("Failed to update pdf_url:", err);
    });

    res.status(200).json({
        message: "PDF generated successfully",
        pdf_url: pdfRelPath,
    });
});

/* ══════════════════════════════════════════════════════════════
   ENDPOINT 3: GET /api/documents/prescription/:id
════════════════════════════════════════════════════════════════ */
exports.getPDF = asyncHandler(async (req, res, next) => {
    const { id } = req.params;
    const { prescription } = await fetchFullPrescription(id);

    if (!prescription) {
        const err = new Error("Prescription not found");
        err.status = 404;
        throw err;
    }

    if (!isAuthorized(prescription, req.user)) {
        const err = new Error("Forbidden: You cannot access this PDF");
        err.status = 403;
        throw err;
    }

    if (!prescription.pdf_url) {
        const err = new Error("PDF has not been generated yet. Call the /generate endpoint first.");
        err.status = 404;
        throw err;
    }

    const absolutePath = path.join(__dirname, "..", prescription.pdf_url);
    res.setHeader("Content-Type", "application/pdf");
    res.setHeader("Content-Disposition", `inline; filename="prescription_${id}.pdf"`);
    res.sendFile(absolutePath, (err) => {
        if (err) next(err);
    });
});
