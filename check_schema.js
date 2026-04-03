const db = require("./db");

async function checkSchema() {
    try {
        db.query("SHOW COLUMNS FROM users LIKE 'status'", (err, res) => {
            if (err) {
                console.error(err);
            } else {
                console.log("STATUS COLUMN:", JSON.stringify(res, null, 2));
            }
            process.exit(0);
        });
    } catch (error) {
        console.error(error);
        process.exit(1);
    }
}

checkSchema();
