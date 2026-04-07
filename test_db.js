const mysql = require('mysql2');
const path = require('path');

// Assuming database connection is established similar to index.js or config
const db = mysql.createPool({
    host: '127.0.0.1',
    user: 'root',
    password: '',
    database: 'diasrx',
    port: 3307
});

db.promise().query("DESCRIBE patients")
    .then(([rows]) => {
        console.log("Patients Table Schema:");
        console.table(rows);
        return db.promise().query("SELECT name, gender FROM patients LIMIT 5");
    })
    .then(([rows]) => {
        console.log("Sample Patients Gender data:");
        console.table(rows);
        process.exit(0);
    })
    .catch(err => {
        console.error(err);
        process.exit(1);
    });
