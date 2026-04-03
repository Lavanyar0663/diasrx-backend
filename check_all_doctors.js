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
        console.log('--- ALL DOCTORS ---');
        console.table(docs);

        const [pats] = await conn.query('SELECT department, COUNT(*) as count FROM patients GROUP BY department');
        console.log('--- PATIENT DEPT STATS ---');
        console.table(pats);

    } catch (err) {
        console.error(err);
    } finally {
        if (conn) await conn.end();
    }
}
run();
