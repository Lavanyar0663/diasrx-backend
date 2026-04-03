const db = require("./db");
const bcrypt = require("bcrypt");

const specializations = ["Cardiology", "Neurology", "Orthopedics", "Pediatrics", "General Medicine", "Dentistry", "Dermatology"];
const titles = ["Junior Doctor", "Senior Doctor", "Consultant"];

async function cleanup() {
    console.log("Starting data cleanup...");

    try {
        // 1. Get all users
        const users = await new Promise((resolve, reject) => {
            db.query("SELECT * FROM users", (err, res) => err ? reject(err) : resolve(res));
        });

        for (const user of users) {
            let updates = [];
            let params = [];

            // Password hashing
            if (user.password && !user.password.startsWith("$2b$")) {
                const hashed = await bcrypt.hash(user.password, 10);
                updates.push("password = ?");
                params.push(hashed);
            }

            // Name defaulting
            if (!user.name) {
                updates.push("name = ?");
                params.push("Unknown User");
            }

            // Age/Gender defaulting
            if (user.age === null) {
                updates.push("age = 0");
            }
            if (!user.gender) {
                updates.push("gender = 'Not Specified'");
            }

            // Professional title defaulting
            if (user.role === 'doctor' && !user.professional_title) {
                const title = titles[Math.floor(Math.random() * titles.length)];
                updates.push("professional_title = ?");
                params.push(title);
            }

            // Phone number de-duplication (naive: add suffix if exists)
            // But first, ensure it's not null
            if (!user.phone) {
                const randomPhone = `91${Math.floor(6000000000 + Math.random() * 3999999999)}`;
                updates.push("phone = ?");
                params.push(randomPhone);
            }

            if (updates.length > 0) {
                params.push(user.id);
                await new Promise((resolve, reject) => {
                    db.query(`UPDATE users SET ${updates.join(", ")} WHERE id = ?`, params, (err) => err ? reject(err) : resolve());
                });
            }

            // 2. Role-specific cleanup
            if (user.role === 'doctor') {
                const spec = specializations[Math.floor(Math.random() * specializations.length)];
                await new Promise((resolve, reject) => {
                    db.query("UPDATE doctors SET specialization = COALESCE(specialization, ?), professional_title = COALESCE(professional_title, ?), phone = COALESCE(phone, ?) WHERE user_id = ?",
                        [spec, user.professional_title || "Senior Doctor", user.phone, user.id], (err) => err ? reject(err) : resolve());
                });
            } else if (user.role === 'patient') {
                // Ensure doctor_id is not null
                const doctors = await new Promise((resolve, reject) => {
                    db.query("SELECT id, specialization FROM doctors LIMIT 1", (err, rows) => err ? reject(err) : resolve(rows));
                });
                if (doctors.length > 0) {
                    await new Promise((resolve, reject) => {
                        db.query("UPDATE patients SET doctor_id = COALESCE(doctor_id, ?), department = COALESCE(department, ?), phone = COALESCE(phone, ?) WHERE name = ? AND phone = ?",
                            [doctors[0].id, doctors[0].specialization, user.phone, user.name, user.phone], (err) => err ? reject(err) : resolve());
                    });
                }
            }
        }

        console.log("Data cleanup complete!");
        process.exit(0);
    } catch (err) {
        console.error("Cleanup failed:", err.message);
        process.exit(1);
    }
}

cleanup();
