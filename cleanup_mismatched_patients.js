const mysql = require('mysql2/promise');
require('dotenv').config({ path: './backend/.env' });

const poolOptions = {
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    port: process.env.DB_PORT
};

async function cleanup() {
    console.log('--- Cleaning Up Mismatched Patient-Doctor Assignments ---');
    try {
        const conn = await mysql.createConnection(poolOptions);
        
        // 1. Identify patients where doctor_id's specialization doesn't match patient's department
        const [mismatched] = await conn.query(`
            SELECT p.id, p.name, p.department, d.name as assigned_doctor, d.specialization as doctor_dept
            FROM patients p
            JOIN doctors d ON p.doctor_id = d.id
            WHERE p.department != d.specialization
        `);
        
        if (mismatched.length === 0) {
            console.log('  No mismatched assignments found.');
        } else {
            console.log(`  Found ${mismatched.length} mismatched assignments.`);
            console.table(mismatched);
            
            // 2. Clear doctor_id for these patients
            const idsToFix = mismatched.map(m => m.id);
            const [updateResult] = await conn.query(
                "UPDATE patients SET doctor_id = NULL WHERE id IN (?)",
                [idsToFix]
            );
            console.log(`  SUCCESS: Cleared doctor_id for ${updateResult.affectedRows} patients.`);
        }
        
        await conn.end();
        console.log('--- Cleanup Complete! ---');
    } catch (err) {
        console.error('  ERROR:', err.message);
    } finally {
        process.exit(0);
    }
}

cleanup();
