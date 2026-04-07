const asyncHandler = require("express-async-handler");
const path = require("path");
const db = require("../db");
const aiExplanationService = require("../services/aiExplanationService");
const pdfGeneratorService = require("../services/pdfGeneratorService");

/* ══════════════════════════════════════════════════════════════
   HELPER: checks if the requesting user is authorized to access
   a given prescription (enforced per role).
════════════════════════════════════════════════════════════════ */
const isAuthorized = async (prescription, user) => {
    if (!prescription) return false;
    if (user.role === "admin") return true;
    if (user.role === "doctor") {
        const [docs] = await db.promise().query("SELECT id FROM doctors WHERE user_id = ?", [user.id]);
        if (docs.length === 0) return false;
        return String(prescription.doctor_id) === String(docs[0].id);
    }
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
        SELECT pd.quantity, pd.frequency, dm.name AS drug_name
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

    if (!(await isAuthorized(prescription, req.user))) {
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

    if (!(await isAuthorized(prescription, req.user))) {
        console.error(`[PDF_DEBUG] Authorization failed for user ${req.user.id} (role: ${req.user.role}) mapping to doctor_id mismatch in prescription ${id}`);
        const err = new Error("Forbidden: You cannot generate a PDF for this prescription");
        err.status = 403;
        throw err;
    }

    try {
        console.log(`[PDF_DEBUG] Building AI explanation for Rx #${id}`);
        const explanation = aiExplanationService.buildExplanation(prescription, drugs);
        
        console.log(`[PDF_DEBUG] Calling PDF Generator for Rx #${id}. Drugs: ${drugs.length}`);
        const pdfRelPath = await pdfGeneratorService.generatePrescriptionPDF(prescription, drugs, explanation);
        
        console.log(`[PDF_DEBUG] PDF generated at: ${pdfRelPath}. Updating DB...`);

        // Persist pdf_url to DB
        await new Promise((resolve, reject) => {
            db.query("UPDATE prescription SET pdf_url = ? WHERE id = ?", [pdfRelPath, id], (err) => {
                if (err) {
                    console.error("[PDF_DEBUG] DB update error:", err);
                    return reject(err);
                }
                resolve();
            });
        });

        res.status(200).json({
            message: "PDF generated successfully",
            pdf_url: pdfRelPath,
        });
    } catch (genErr) {
        console.error(`[PDF_DEBUG] FATAL ERROR GENERATING PDF for Rx #${id}:`, genErr);
        res.status(500).json({ 
            message: "Failed to generate PDF", 
            details: genErr.message 
        });
    }
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

    if (!(await isAuthorized(prescription, req.user))) {
        console.error(`[PDF_DEBUG] Authorization failed for user ${req.user.id} on prescription ${id}`);
        const err = new Error("Forbidden: You cannot access this PDF");
        err.status = 403;
        throw err;
    }

    if (!prescription.pdf_url) {
        console.error(`[PDF_DEBUG] PDF status check failed for ${id}: URL missing in DB`);
        const err = new Error("PDF has not been generated yet. Please try again in a moment.");
        err.status = 404;
        throw err;
    }

    // Use path.resolve to get a absolute path properly on Windows
    const absolutePath = path.resolve(__dirname, "..", prescription.pdf_url);
    console.log(`[PDF_DEBUG] Final Absolute Path: ${absolutePath}`);

    if (!require("fs").existsSync(absolutePath)) {
        console.error(`[PDF_DEBUG] File NOT FOUND on disk at: ${absolutePath}`);
        const err = new Error("Generated PDF file not found on server storage.");
        err.status = 404;
        throw err;
    }
    
    res.setHeader("Content-Type", "application/pdf");
    res.setHeader("Content-Disposition", `attachment; filename="prescription_${id}.pdf"`);
    
    res.sendFile(absolutePath, (err) => {
        if (err) {
            console.error(`[PDF_DEBUG] res.sendFile error for ${id}:`, err);
            if (!res.headersSent) {
                res.status(500).json({ message: "Failed to send file" });
            }
        }
    });
});
