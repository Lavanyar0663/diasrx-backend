const mysql = require('mysql2/promise');
const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '.env') });

async function run() {
    let conn;
    try {
        conn = await mysql.createConnection({
            host: process.env.DB_HOST || '127.0.0.1',
            user: process.env.DB_USER || 'root',
            password: process.env.DB_PASSWORD || '',
            database: process.env.DB_NAME || 'diasrx',
            port: process.env.DB_PORT || 3307
        });

        const email = 'arjun.diasrx.com';
        const [users] = await conn.query('SELECT id, name FROM users WHERE email = ?', [email]);
        if (users.length === 0) { console.log('USER NOT FOUND'); return; }
        
        const [docs] = await conn.query('SELECT id, specialization FROM doctors WHERE user_id = ?', [users[0].id]);
        if (docs.length === 0) { console.log('DOC NOT FOUND'); return; }
        
        const doc = docs[0];
        console.log(`Doctor: ${users[0].name}, Spec: "${doc.specialization}"`);

        const sql = "SELECT COUNT(*) as c FROM patients WHERE is_active = 1 AND (doctor_id = ? OR (doctor_id IS NULL AND department = ?))";
        const [count] = await conn.query(sql, [doc.id, doc.specialization]);
        console.log('VISIBLE PATIENTS COUNT:', count[0].c);

        const [sample] = await conn.query("SELECT name, department, doctor_id FROM patients WHERE is_active = 1 AND (doctor_id = ? OR (doctor_id IS NULL AND department = ?)) LIMIT 5", [doc.id, doc.specialization]);
        console.log('SAMPLE:', JSON.stringify(sample, null, 2));

    } catch (err) {
        console.error(err);
    } finally {
        if (conn) await conn.end();
    }
}
run();
