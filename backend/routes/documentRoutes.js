const express = require("express");
const documentController = require("../controllers/documentController");
const authMiddleware = require("../middleware/authMiddleware");
const roleMiddleware = require("../middleware/roleMiddleware");

const router = express.Router();

// Generate a PDF for a prescription (Doctor or Admin only)
router.post(
    "/prescription/:id/generate",
    authMiddleware,
    roleMiddleware(["doctor", "admin"]),
    documentController.generatePDF
);

// Retrieve / stream an existing PDF (Doctor, Admin, or Patient own)
router.get(
    "/prescription/:id",
    authMiddleware,
    roleMiddleware(["doctor", "admin", "patient"]),
    documentController.getPDF
);

module.exports = router;
