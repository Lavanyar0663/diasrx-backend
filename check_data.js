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

        // 1. Find Arjun
        const [users] = await conn.query('SELECT id, name, email, role FROM users WHERE email LIKE ?', ['%arjun%']);
        console.log('--- USERS ---');
        console.log(users);

        if (users.length > 0) {
            const user = users[0];
            // 2. Check Doctor Profile
            const [docs] = await conn.query('SELECT id, user_id, name, specialization FROM doctors WHERE user_id = ?', [user.id]);
            console.log('--- DOCTOR PROFILE ---');
            console.log(docs);

            if (docs.length > 0) {
                const doc = docs[0];
                console.log(`Doctor specialization: "${doc.specialization}"`);

                // 3. See patients matching his ID or his specialization (if doctor_id is NULL)
                const [pats] = await conn.query(
                    'SELECT id, name, department, doctor_id FROM patients WHERE doctor_id = ? OR (doctor_id IS NULL AND department = ?)',
                    [doc.id, doc.specialization]
                );
                console.log('--- VISIBLE PATIENTS ---');
                console.log(pats);
            }
        }

        // 4. See unique departments in patients table
        const [depts] = await conn.query('SELECT DISTINCT department FROM patients');
        console.log('--- PATIENT DEPARTMENTS ---');
        console.log(depts.map(d => d.department));

    } catch (err) {
        console.error('Error:', err);
    } finally {
        if (conn) await conn.end();
    }
}

run();
