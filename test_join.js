const mysql = require('mysql2');

const db = mysql.createPool({
    host: '127.0.0.1',
    user: 'root',
    password: '',
    database: 'diasrx',
    port: 3307
});

const sql = `
    SELECT p.*, pat.name as patient_name, pat.id as patient_display_id, pat.gender
    FROM prescription p
    JOIN patients pat ON p.patient_id = pat.id
    LIMIT 5`;

db.promise().query(sql)
    .then(([rows]) => {
        console.log("SQL Query Result (Join Test):");
        console.table(rows.map(r => ({
            id: r.id,
            patient_name: r.patient_name,
            gender: r.gender
        })));
        process.exit(0);
    })
    .catch(err => {
        console.error(err);
        process.exit(1);
    });
