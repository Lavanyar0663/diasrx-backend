require("dotenv").config();
const mysql = require("mysql2");
const fs = require('fs');

const db = mysql.createConnection({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    port: process.env.DB_PORT || 3307
});

db.query("SELECT * FROM drug_master WHERE name LIKE '%Paracetamol%'", (err, result) => {
    if (err) {
        fs.writeFileSync('para_error.txt', err.message);
        process.exit(1);
    }
    fs.writeFileSync('para_results.json', JSON.stringify(result, null, 2));
    db.end();
});
