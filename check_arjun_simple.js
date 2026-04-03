const mysql = require('mysql');
require('dotenv').config({ path: './backend/.env' });

const db = mysql.createConnection({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    port: process.env.DB_PORT
});

db.query('SELECT id, email, name FROM users WHERE email LIKE "%arjun%"', (err, users) => {
    if (err) throw err;
    console.log('--- USER ---');
    console.log(users);
    if (users.length > 0) {
        db.query('SELECT specialization FROM doctors WHERE user_id = ?', [users[0].id], (err, docs) => {
            if (err) throw err;
            console.log('--- DOCTOR ---');
            console.log(docs);
            const spec = docs[0] ? docs[0].specialization : 'NONE';
            
            db.query('SELECT DISTINCT department FROM patients', (err, depts) => {
                if (err) throw err;
                console.log('--- PATIENT DEPTS ---');
                console.log(depts.map(d => d.department));
                
                db.query('SELECT COUNT(*) as count FROM patients WHERE doctor_id = ? OR (doctor_id IS NULL AND department = ?)', 
                [docs[0].id, spec], (err, results) => {
                    if (err) throw err;
                    console.log('--- MATCH COUNT ---');
                    console.log(results);
                    process.exit(0);
                });
            });
        });
    } else {
        process.exit(0);
    }
});
