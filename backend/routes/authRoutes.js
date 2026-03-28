const express = require("express");
const authController = require("../controllers/authController");
const authMiddleware = require("../middleware/authMiddleware");
const roleMiddleware = require("../middleware/roleMiddleware");
const { authLimiter } = require("../middleware/rateLimitMiddleware");

const router = express.Router();

// Strict rate-limited login endpoint
router.post("/login", authLimiter, authController.login);

// Admin-only user registration endpoint
router.post("/register", authMiddleware, roleMiddleware(["admin"]), authController.register);

// Forgot password routes
router.post("/forgot-password", authController.forgotPassword);
router.post("/verify-otp", authController.verifyOtp);
router.post("/reset-password", authController.resetPassword);
router.patch("/change-password", authMiddleware, authController.changePassword);

// New Request Access route (Public)
router.post("/request-access", authController.requestAccess);

module.exports = router;