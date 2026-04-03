require("dotenv").config();
const mysql = require("mysql2");

const db = mysql.createConnection({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    port: process.env.DB_PORT || 3306
});

db.query("SELECT name, COUNT(*) as count FROM drug_master GROUP BY name HAVING count > 1", (err, result) => {
    if (err) {
        console.error("Error:", err.message);
        process.exit(1);
    }
    console.log(JSON.stringify(result, null, 2));
    db.end();
});
