const db = require("./db");
const bcrypt = require("bcrypt");

async function cleanup() {
    console.log("Starting final database cleanup...");

    try {
        // 1. Get all users
        const users = await new Promise((resolve, reject) => {
            db.query("SELECT * FROM users", (err, res) => err ? reject(err) : resolve(res));
        });

        for (const user of users) {
            let updates = [];
            let params = [];

            // Skip hashing if already hashed
            if (user.password && !user.password.startsWith("$2b$")) {
                const hashedPassword = await bcrypt.hash(user.password, 10);
                updates.push("password = ?");
                params.push(hashedPassword);
                console.log(`Hashing password for user: ${user.name}`);
            }

            // Ensure name is not null
            if (!user.name) {
                updates.push("name = ?");
                params.push("Unknown User");
            }

            // Ensure role is not null
            if (!user.role) {
                updates.push("role = ?");
                params.push("patient");
            }

            // Generate email if missing OR if it looks like a placeholder
            if (!user.email || user.email.includes("random") || user.email.includes("doctor_17")) {
                const baseEmail = (user.name || "user").toLowerCase().replace(/\s+/g, ".");
                let generatedEmail = `${baseEmail}@diasrx.com`;
                
                // Check for uniqueness
                let suffix = 1;
                let exists = true;
                while (exists) {
                    const check = await new Promise((resolve, reject) => {
                        db.query("SELECT id FROM users WHERE email = ? AND id != ?", [generatedEmail, user.id], (err, res) => err ? reject(err) : resolve(res));
                    });
                    if (check.length === 0) {
                        exists = false;
                    } else {
                        generatedEmail = `${baseEmail}${suffix}@diasrx.com`;
                        suffix++;
                    }
                }
                updates.push("email = ?");
                params.push(generatedEmail);
                console.log(`Generated email ${generatedEmail} for ${user.name}`);
            }

            // Ensure phone is not null
            if (!user.phone) {
                updates.push("phone = ?");
                params.push(`99999${Math.floor(10000 + Math.random() * 90000)}`);
            }

            if (updates.length > 0) {
                params.push(user.id);
                await new Promise((resolve, reject) => {
                    db.query(`UPDATE users SET ${updates.join(", ")} WHERE id = ?`, params, (err, res) => err ? reject(err) : resolve(res));
                });
            }

            // 2. Sync to role-specific tables if approved
            if (user.status === 'APPROVED' || user.role === 'admin') {
                if (user.role === 'doctor') {
                    const docs = await new Promise((resolve, reject) => {
                        db.query("SELECT id FROM doctors WHERE user_id = ?", [user.id], (err, res) => err ? reject(err) : resolve(res));
                    });
                    if (docs.length === 0) {
                        await new Promise((resolve, reject) => {
                            db.query("INSERT INTO doctors (user_id, name, email, phone, specialization, professional_title) VALUES (?, ?, ?, ?, ?, ?)",
                                [user.id, user.name, user.email, user.phone, user.department || 'General', user.professional_title || 'Doctor'], (err, res) => err ? reject(err) : resolve(res));
                        });
                        console.log(`Synced doctor: ${user.name}`);
                    }
                } else if (user.role === 'pharmacist') {
                    const pharms = await new Promise((resolve, reject) => {
                        db.query("SELECT id FROM pharmacists WHERE user_id = ?", [user.id], (err, res) => err ? reject(err) : resolve(res));
                    });
                    if (pharms.length === 0) {
                        await new Promise((resolve, reject) => {
                            db.query("INSERT INTO pharmacists (user_id, name, email, phone) VALUES (?, ?, ?, ?)",
                                [user.id, user.name, user.email, user.phone], (err, res) => err ? reject(err) : resolve(res));
                        });
                        console.log(`Synced pharmacist: ${user.name}`);
                    }
                } else if (user.role === 'patient') {
                    const pats = await new Promise((resolve, reject) => {
                        db.query("SELECT id FROM patients WHERE phone = ? OR email = ?", [user.phone, user.email], (err, res) => err ? reject(err) : resolve(res));
                    });
                    if (pats.length === 0) {
                        await new Promise((resolve, reject) => {
                            db.query("INSERT INTO patients (name, email, age, gender, phone, department, is_active) VALUES (?, ?, ?, ?, ?, ?, 1)",
                                [user.name, user.email, 30, 'Unknown', user.phone, user.department || 'General'], (err, res) => err ? reject(err) : resolve(res));
                        });
                        console.log(`Synced patient: ${user.name}`);
                    }
                }
            }
        }

        console.log("Database cleanup and verification complete!");
        process.exit(0);
    } catch (err) {
        console.error("Cleanup failed:", err);
        process.exit(1);
    }
}

cleanup();
