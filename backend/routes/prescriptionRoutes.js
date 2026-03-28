const express = require("express");
const prescriptionController = require("../controllers/prescriptionController");
const documentController = require("../controllers/documentController");
const authMiddleware = require("../middleware/authMiddleware");
const roleMiddleware = require("../middleware/roleMiddleware");
const idempotencyMiddleware = require("../middleware/idempotencyMiddleware");

const router = express.Router();

// Create full prescription (atomic: prescription + items)
router.post(
  "/full",
  authMiddleware,
  roleMiddleware(["doctor"]),
  prescriptionController.createFullPrescription
);

// Dispense prescription with strict validation and stock protection
router.patch(
  "/:id/dispense",
  authMiddleware,
  roleMiddleware(["pharmacist"]),
  idempotencyMiddleware,
  prescriptionController.dispensePrescription
);

// Fetch all pending prescriptions (Pharmacist/Admin)
router.get(
  "/pending",
  authMiddleware,
  roleMiddleware(["pharmacist", "admin"]),
  prescriptionController.getPendingPrescriptions
);

// Fetch all prescription history (Pharmacist/Admin)
router.get(
  "/history",
  authMiddleware,
  roleMiddleware(["pharmacist", "admin"]),
  prescriptionController.getPrescriptionHistory
);

// Fetch stats for Pharmacist dashboard
router.get(
  "/stats",
  authMiddleware,
  roleMiddleware(["pharmacist"]),
  prescriptionController.getPharmacistStats
);

// Fetch prescriptions by patient
router.get(
  "/patient/:id",
  authMiddleware,
  roleMiddleware(["doctor", "admin", "pharmacist"]),
  prescriptionController.getPrescriptionsByPatient
);

// Fetch prescriptions by doctor
router.get(
  "/doctor/:id",
  authMiddleware,
  roleMiddleware(["doctor", "admin"]),
  prescriptionController.getPrescriptionsByDoctor
);

// Get plain-language / AI explanation for a single prescription
// Doctors (own), Admins (all), Patients (own)
router.get(
  "/:id/explained",
  authMiddleware,
  roleMiddleware(["doctor", "admin", "patient"]),
  documentController.getExplanation
);

// Fetch single prescription by ID (Pharmacist/Admin)
router.get(
  "/:id",
  authMiddleware,
  roleMiddleware(["pharmacist", "admin"]),
  prescriptionController.getPrescriptionById
);

module.exports = router;