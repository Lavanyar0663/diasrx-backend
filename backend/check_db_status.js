const db = require("./db");

async function checkDatabase() {
    try {
        db.query("SHOW TABLES", (err, rows) => {
            if (err) {
                console.error("Error showing tables:", err);
                process.exit(1);
            }
            console.log("Tables in database:", rows.map(r => Object.values(r)[0]));

            db.query("DESCRIBE users", (err, columns) => {
                if (err) {
                    console.log("Users table does not exist or error describing it.");
                } else {
                    console.log("Users table structure:", columns.map(c => c.Field));
                }

                db.query("SELECT COUNT(*) as count FROM users", (err, result) => {
                    if (err) {
                        console.log("Could not count users.");
                    } else {
                        console.log("User count:", result[0].count);
                    }
                    process.exit(0);
                });
            });
        });
    } catch (error) {
        console.error("Script error:", error);
        process.exit(1);
    }
}

checkDatabase();
