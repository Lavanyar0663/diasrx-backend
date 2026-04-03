const db = require("./db");

async function migrate() {
    try {
        console.log("[MIGRATION] Checking for isTwoFactorEnabled and isNotificationsEnabled columns in 'users' table...");
        
        const [rows] = await new Promise((resolve, reject) => {
            db.query("DESCRIBE users", (err, result) => {
                if (err) return reject(err);
                resolve([result]);
            });
        });

        const columns = rows.map(r => r.Field);
        
        if (!columns.includes("isTwoFactorEnabled")) {
            console.log("[MIGRATION] Adding 'isTwoFactorEnabled' column to 'users' table...");
            await new Promise((resolve, reject) => {
                db.query("ALTER TABLE users ADD COLUMN isTwoFactorEnabled TINYINT(1) DEFAULT 1", (err) => {
                    if (err) return reject(err);
                    resolve();
                });
            });
        }

        if (!columns.includes("isNotificationsEnabled")) {
            console.log("[MIGRATION] Adding 'isNotificationsEnabled' column to 'users' table...");
            await new Promise((resolve, reject) => {
                db.query("ALTER TABLE users ADD COLUMN isNotificationsEnabled TINYINT(1) DEFAULT 1", (err) => {
                    if (err) return reject(err);
                    resolve();
                });
            });
        }

        console.log("[MIGRATION] Successfully updated 'users' table schema.");
        process.exit(0);
    } catch (err) {
        console.error("[MIGRATION] FAILED:", err.message);
        process.exit(1);
    }
}

migrate();
