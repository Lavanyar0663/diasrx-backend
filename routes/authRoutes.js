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

// First-time password set for approved users
router.post("/set-password", authController.setPassword);

// Check if a user is pre-created (by email) — used by RequestAccessActivity (Public)
router.get("/check-user", authController.checkPreCreatedUser);

// User profile and settings
router.get("/user-settings", authMiddleware, authController.getUserSettings);
router.post("/update-user-settings", authMiddleware, authController.updateUserSettings);
router.patch("/profile", authMiddleware, authController.updateProfile);

module.exports = router;