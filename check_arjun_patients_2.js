const mysql = require('mysql2/promise');
const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '.env') });

async function run() {
    let conn;
    try {
        console.log('Connecting with:', {
            host: process.env.DB_HOST,
            user: process.env.DB_USER,
            database: process.env.DB_NAME,
            port: process.env.DB_PORT
        });

        conn = await mysql.createConnection({
            host: process.env.DB_HOST || '127.0.0.1',
            user: process.env.DB_USER || 'root',
            password: process.env.DB_PASSWORD || '',
            database: process.env.DB_NAME || 'diasrx',
            port: process.env.DB_PORT || 3307
        });

        const email = 'arjun.diasrx.com';
        const [users] = await conn.query('SELECT id, name FROM users WHERE email = ?', [email]);
        if (users.length === 0) {
            console.log('Arjun user not found');
            return;
        }
        const userId = users[0].id;
        console.log(`Arjun User ID: ${userId}, Name: ${users[0].name}`);

        const [doctors] = await conn.query('SELECT id, specialization FROM doctors WHERE user_id = ?', [userId]);
        if (doctors.length === 0) {
            console.log('Arjun doctor profile not found');
            return;
        }
        const docId = doctors[0].id;
        const spec = doctors[0].specialization;
        console.log(`Arjun Doctor ID: ${docId}, Specialization: "${spec}"`);

        // Exact query logic from patientController.js
        const sql = `
            SELECT p.id, p.name, p.department, p.doctor_id
            FROM patients p
            WHERE p.is_active = 1 
            AND (
                p.doctor_id = ?
                OR 
                (p.doctor_id IS NULL AND p.department = ?)
            )
        `;
        const [pats] = await conn.query(sql, [docId, spec]);
        console.log(`Total patients found for Arjun: ${pats.length}`);
        console.table(pats);

        const [unassigned] = await conn.query('SELECT id, name, department FROM patients WHERE doctor_id IS NULL');
        console.log('--- ALL UNASSIGNED PATIENTS ---');
        console.table(unassigned);

    } catch (err) {
        console.error('Error:', err);
    } finally {
        if (conn) await conn.end();
    }
}

run();
