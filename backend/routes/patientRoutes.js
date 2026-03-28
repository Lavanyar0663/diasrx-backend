const express = require("express");
const patientController = require("../controllers/patientController");
const authMiddleware = require("../middleware/authMiddleware");
const roleMiddleware = require("../middleware/roleMiddleware");

const router = express.Router();

router.get("/", authMiddleware, roleMiddleware(["admin", "doctor"]), patientController.getPatients);
router.post("/", authMiddleware, roleMiddleware(["admin", "doctor"]), patientController.addPatient);
router.patch("/:id/assign", authMiddleware, roleMiddleware(["admin"]), patientController.assignDoctor);

module.exports = router;