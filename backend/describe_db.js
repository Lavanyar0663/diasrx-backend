const db = require("./db");

const tables = ["users", "doctors", "pharmacists", "patients", "prescription", "prescription_drugs", "drug_master"];

async function describeTables() {
    for (const table of tables) {
        console.log(`--- Table: ${table} ---`);
        await new Promise((resolve, reject) => {
            db.query(`DESCRIBE ${table}`, (err, results) => {
                if (err) {
                    console.error(`Error describing ${table}:`, err.message);
                    resolve();
                } else {
                    console.table(results);
                    resolve();
                }
            });
        });
    }
    process.exit(0);
}

describeTables();
