const express = require("express");
const doctorController = require("../controllers/doctorController");
const authMiddleware = require("../middleware/authMiddleware");

const router = express.Router();

router.get("/", authMiddleware, doctorController.getDoctors);
router.get("/profile", authMiddleware, doctorController.getProfile);
router.patch("/profile", authMiddleware, doctorController.updateProfile);

module.exports = router;