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
