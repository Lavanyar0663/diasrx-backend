const db = require("./db");

const sql = "ALTER TABLE users MODIFY COLUMN status ENUM('PENDING','APPROVED','REJECTED','INITIATED') DEFAULT 'INITIATED'";

db.query(sql, (err, result) => {
    if (err) {
        console.error("Migration failed:", err.message);
        process.exit(1);
    }
    console.log("Migration successful:", result);
    process.exit(0);
});
