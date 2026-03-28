require("dotenv").config();
const mysql = require("mysql2");

const db = mysql.createConnection({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    port: process.env.DB_PORT || 3306
});

db.query("SELECT * FROM drug_master LIMIT 1", (err, result) => {
    if (err) {
        console.error("Error:", err.message);
        process.exit(1);
    }
    if (result.length > 0) {
        console.log(Object.keys(result[0]).join(", "));
    } else {
        console.log("Empty table");
    }
    db.end();
});
