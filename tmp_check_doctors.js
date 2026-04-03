const mysql = require('mysql2/promise');
require('dotenv').config({ path: './backend/.env' });

async function run() {
    try {
        const pool = await mysql.createConnection({
            host: process.env.DB_HOST,
            user: process.env.DB_USER,
            password: process.env.DB_PASSWORD,
            database: process.env.DB_NAME,
            port: process.env.DB_PORT
        });
        
        console.log('--- DOCTORS ---');
        const [doctors] = await pool.query('SELECT id, name, specialization FROM doctors');
        console.table(doctors);
        
        console.log('--- RECENT PATIENTS ---');
        const [patients] = await pool.query('SELECT id, name, department, doctor_id FROM patients ORDER BY id DESC LIMIT 5');
        console.table(patients);
        
        await pool.end();
    } catch (e) {
        console.error(e);
    }
}
run();
