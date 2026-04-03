const mysql = require('mysql2/promise');
require('dotenv').config();

async function run() {
    let conn;
    try {
        conn = await mysql.createConnection({
            host: process.env.DB_HOST,
            user: process.env.DB_USER,
            password: process.env.DB_PASSWORD,
            database: process.env.DB_NAME,
            port: process.env.DB_PORT
        });

        const [docs] = await conn.query('SELECT name, specialization, user_id FROM doctors');
        console.log('--- DOCTORS ---');
        docs.forEach(d => console.log(`${d.name} | Spec: ${d.specialization}`));

        const [pats] = await conn.query('SELECT department, COUNT(*) as c FROM patients GROUP BY department');
        console.log('--- PATIENT DEPTS ---');
        pats.forEach(p => console.log(`${p.department}: ${p.c}`));

    } catch (err) {
        console.error(err);
    } finally {
        if (conn) await conn.end();
    }
}
run();
