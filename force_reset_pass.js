const db = require("./db");
const bcrypt = require("bcrypt");

async function resetPasswords() {
    try {
        const password = "password123";
        const hash = await bcrypt.hash(password, 10);
        console.log(`Setting all passwords to: ${password} (Hash: ${hash})`);

        const tables = ["users", "doctors", "pharmacists", "admin"];
        
        for (const table of tables) {
            await new Promise((resolve, reject) => {
                db.query(`UPDATE ${table} SET password = ?`, [hash], (err, result) => {
                    if (err) {
                        console.error(`Error updating ${table}:`, err.message);
                        resolve(); // continue anyway
                    } else {
                        console.log(`Updated ${result.affectedRows} rows in ${table}`);
                        resolve();
                    }
                });
            });
        }
        
        console.log("Password reset complete!");
        process.exit(0);
    } catch (err) {
        console.error("Reset failed:", err);
        process.exit(1);
    }
}

resetPasswords();
