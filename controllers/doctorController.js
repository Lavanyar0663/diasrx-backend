const asyncHandler = require("express-async-handler");
const db = require("../db");

exports.getDoctors = (req, res, next) => {
  const sql = `
    SELECT 
      id,
      name,
      email,
      specialization,
      phone,
      professional_title,
      avatar_url,
      created_at
    FROM doctors
  `;

  db.query(sql, (err, result) => {
    if (err) return next(err);
    res.status(200).json(result);
  });
};

exports.getProfile = (req, res, next) => {
  const userId = req.user.id;
  const sql = "SELECT * FROM doctors WHERE user_id = ?";
  
  db.query(sql, [userId], (err, result) => {
    if (err) return next(err);
    if (result.length === 0) return res.status(404).json({ message: "Doctor not found" });
    res.status(200).json(result[0]);
  });
};

exports.updateProfile = (req, res, next) => {
  const userId = req.user.id;
  const { name, specialization, phone, professional_title, avatar_url } = req.body;
  
  const sql = `
    UPDATE doctors 
    SET name = ?, specialization = ?, phone = ?, professional_title = ?, avatar_url = ?
    WHERE user_id = ?
  `;
  
  db.query(sql, [name, specialization, phone, professional_title, avatar_url, userId], (err, result) => {
    if (err) {
      console.error("Error updating doctor profile:", err);
      return next(err);
    }
    
    // Also update name in users table for consistency
    const updateUsersSql = "UPDATE users SET name = ? WHERE id = ?";
    db.query(updateUsersSql, [name, userId], (err2) => {
        if (err2) console.error("Failed to update user name in users table:", err2);
        res.status(200).json({ message: "Profile updated successfully" });
    });
  });
};

exports.getStats = asyncHandler(async (req, res, next) => {
    const userId = req.user.id;

    // 1. Map users.id to doctors.id
    const [docResult] = await db.promise().query("SELECT id FROM doctors WHERE user_id = ?", [userId]);
    if (docResult.length === 0) {
        return res.status(404).json({ message: "Doctor record not found" });
    }
    const doctorId = docResult[0].id;

    // 2. Fetch counts
    const [patientCount] = await db.promise().query("SELECT COUNT(*) as total FROM patients WHERE doctor_id = ? AND is_active = 1", [doctorId]);
    const [pendingCount] = await db.promise().query(
        "SELECT COUNT(*) as total FROM prescription WHERE doctor_id = ? AND (status IN ('PENDING', 'CREATED', '', NULL) OR status IS NULL)", 
        [doctorId]
    );
    const [dispensedCount] = await db.promise().query(
        "SELECT COUNT(*) as total FROM prescription WHERE doctor_id = ? AND status = 'DISPENSED'", 
        [doctorId]
    );

    console.log(`[GET_STATS] user.id: ${userId}, resolved doctor_id: ${doctorId}. P:${patientCount[0].total}, Pend:${pendingCount[0].total}, Disp:${dispensedCount[0].total}`);

    res.status(200).json({
        totalPatients: patientCount[0].total,
        pendingPrescriptions: pendingCount[0].total,
        dispensedPrescriptions: dispensedCount[0].total
    });
});

exports.getDashboardInfo = asyncHandler(async (req, res, next) => {
    const userId = req.user.id;

    // 1. Fetch doctor details
    const [docResult] = await db.promise().query("SELECT id, name, specialization FROM doctors WHERE user_id = ?", [userId]);
    if (docResult.length === 0) {
        return res.status(404).json({ message: "Doctor record not found" });
    }
    const doctor = docResult[0];

    // 2. Fetch TODAY's OPD load (pending/created prescriptions created today only)
    const [pendingCount] = await db.promise().query(
        `SELECT COUNT(*) as total FROM prescription 
         WHERE doctor_id = ? 
         AND (status IN ('PENDING', 'CREATED', 'ISSUED') OR status IS NULL)
         AND DATE(created_at) = CURDATE()`, 
        [doctor.id]
    );

    // 3. Clean doctor name - strip all redundant prefixes
    let doctorName = doctor.name || "Doctor";
    doctorName = doctorName.replace(/^(dr\.?\s*\.?\s*)+/gi, "").replace(/^[\.\s]+/, "").trim();

    // 4. Return dynamic dashboard info
    res.status(200).json({
        doctorName: doctorName,
        department: doctor.specialization || "General",
        opdLoad: pendingCount[0].total,
    });
});
