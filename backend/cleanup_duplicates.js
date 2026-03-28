require("dotenv").config();
const mysql = require("mysql2");

const db = mysql.createConnection({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    port: process.env.DB_PORT || 3307
});

async function cleanup() {
    console.log("Starting deduplication cleanup...");
    try {
        // 1. Delete duplicate rows, keeping only the one with the smallest ID
        const deleteRes = await db.promise().query(`
            DELETE d1 FROM drug_master d1
            INNER JOIN drug_master d2 
            WHERE d1.id > d2.id AND d1.name = d2.name
        `);
        console.log(`Removed ${deleteRes[0].affectedRows} duplicate rows.`);

        // 2. Add UNIQUE constraint to prevent future duplicates
        console.log("Adding UNIQUE constraint to 'name' column...");
        await db.promise().query("ALTER TABLE drug_master ADD UNIQUE (name)");
        console.log("UNIQUE constraint added successfully.");

    } catch (err) {
        if (err.code === "ER_DUP_ENTRY" || err.message.contains("Duplicate key name")) {
            console.log("Constraint might already exist or duplicates remain.");
        }
        console.error("Error during cleanup:", err.message);
    } finally {
        db.end();
    }
}

cleanup();
