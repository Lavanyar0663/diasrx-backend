const db = require("./db");

async function fixSchema() {
    try {
        console.log("[FIX] Checking columns in 'users' table...");
        
        const [rows] = await new Promise((resolve, reject) => {
            db.query("DESCRIBE users", (err, result) => {
                if (err) return reject(err);
                resolve([result]);
            });
        });

        const columns = rows.map(r => r.Field);
        const requiredColumns = [
            "isTwoFactorEnabled",
            "isNotificationsEnabled",
            "isWhatsAppEnabled",
            "isEmailEnabled",
            "isSecurityAlertsEnabled",
            "isLatencyEnabled",
            "isOnboardingEnabled"
        ];

        for (const col of requiredColumns) {
            if (!columns.includes(col)) {
                console.log(`[FIX] Adding missing column: ${col}`);
                await new Promise((resolve, reject) => {
                    db.query(`ALTER TABLE users ADD COLUMN ${col} TINYINT(1) DEFAULT 1`, (err) => {
                        if (err) return reject(err);
                        resolve();
                    });
                });
            } else {
                console.log(`[FIX] Column already exists: ${col}`);
            }
        }

        console.log("[FIX] Schema update complete.");
        process.exit(0);
    } catch (err) {
        console.error("[FIX] FAILED:", err.message);
        process.exit(1);
    }
}

fixSchema();
