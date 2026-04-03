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

        console.log('--- ALIGNING DEPARTMENT NAMES ---');

        // 1. Update Doctors
        const [docResult] = await conn.query(
            "UPDATE doctors SET specialization = 'General Dentistry' WHERE specialization = 'General Medicine'"
        );
        console.log(`Updated ${docResult.affectedRows} doctors from "General Medicine" to "General Dentistry"`);

        // 2. Update Patients
        const [patResult] = await conn.query(
            "UPDATE patients SET department = 'General Dentistry' WHERE department = 'General Medicine'"
        );
        console.log(`Updated ${patResult.affectedRows} patients from "General Medicine" to "General Dentistry"`);

        // 3. Update Users (for consistency if applicable)
        const [userResult] = await conn.query(
            "UPDATE users SET department = 'General Dentistry' WHERE department = 'General Medicine'"
        );
        console.log(`Updated ${userResult.affectedRows} user records for consistency`);

        console.log('\nSUCCESS: All "General Medicine" references have been aligned to "General Dentistry".');

    } catch (err) {
        console.error('Migration failed:', err);
    } finally {
        if (conn) await conn.end();
    }
}

run();
