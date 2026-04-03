const asyncHandler = require("express-async-handler");
const db = require("../db");

const checkIdempotency = asyncHandler(async (req, res, next) => {
    const idempotencyKey = req.header("Idempotency-Key");
    const prescriptionId = req.params.id;

    if (!idempotencyKey) {
        const err = new Error("Idempotency-Key header is required");
        err.status = 400;
        throw err;
    }

    const sql = `
    SELECT * FROM dispense_logs 
    WHERE prescription_id = ? AND idempotency_key = ?
  `;

    const result = await new Promise((resolve, reject) => {
        db.query(sql, [prescriptionId, idempotencyKey], (err, rows) => {
            if (err) return reject(err);
            resolve(rows);
        });
    });

    if (result.length > 0) {
        // Key already used successfully for this prescription
        return res.status(200).json({
            message: "Prescription already dispensed successfully (Idempotency matched)",
            prescription_id: prescriptionId
        });
    }

    // Attach key to request for the controller to use
    req.idempotencyKey = idempotencyKey;
    next();
});

module.exports = checkIdempotency;
