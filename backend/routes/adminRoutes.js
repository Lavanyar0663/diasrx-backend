const express = require("express");
const router = express.Router();
const adminController = require("../controllers/adminController");
const authMiddleware = require("../middleware/authMiddleware");
const roleMiddleware = require("../middleware/roleMiddleware");

// All admin routes are protected by auth and role middleware
router.use(authMiddleware, roleMiddleware(["admin"]));

// Get all pending requests
router.get("/requests/all", adminController.getAllRequests);
router.get("/requests", adminController.getPendingRequests);

// Approve a request
router.patch("/requests/:id/approve", adminController.approveRequest);

// Reject a request
router.patch("/requests/:id/reject", adminController.rejectRequest);

// Get counts for dashboard cards
router.get("/stats", adminController.getAdminStats);

// Get staff lists
router.get("/doctors", adminController.getDoctors);
router.get("/pharmacists", adminController.getPharmacists);

module.exports = router;
