const db = require("./db");

async function migrate() {
    const queries = [
        "ALTER TABLE doctors ADD COLUMN is_active TINYINT(1) DEFAULT 0",
        "ALTER TABLE pharmacists ADD COLUMN is_active TINYINT(1) DEFAULT 0",
        "UPDATE doctors SET is_active = 1 WHERE user_id IN (SELECT id FROM users WHERE status = 'APPROVED')",
        "UPDATE pharmacists SET is_active = 1 WHERE user_id IN (SELECT id FROM users WHERE status = 'APPROVED')"
    ];

    for (const sql of queries) {
        try {
            await new Promise((resolve, reject) => {
                db.query(sql, (err, res) => err ? reject(err) : resolve(res));
            });
            console.log(`[SUCCESS] Executed: ${sql}`);
        } catch (err) {
            console.error(`[ERROR] Failed: ${sql}\nReason: ${err.message}`);
        }
    }
    process.exit();
}

migrate();
