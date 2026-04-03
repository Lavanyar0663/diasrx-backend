const express = require("express");
const doctorController = require("../controllers/doctorController");
const authMiddleware = require("../middleware/authMiddleware");

const router = express.Router();

router.get("/", authMiddleware, doctorController.getDoctors);
router.get("/profile", authMiddleware, doctorController.getProfile);
router.patch("/profile", authMiddleware, doctorController.updateProfile);
router.get("/stats", authMiddleware, doctorController.getStats);
router.get("/dashboard-info", authMiddleware, doctorController.getDashboardInfo);

module.exports = router;