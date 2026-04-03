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

        // 1. Get Arjun's Specialization
        const [users] = await conn.query('SELECT id FROM users WHERE email = ?', ['arjun@diasrx.com']);
        if (users.length === 0) { console.log('USER NOT FOUND'); return; }
        
        const [docs] = await conn.query('SELECT specialization FROM doctors WHERE user_id = ?', [users[0].id]);
        const spec = docs[0].specialization;
        console.log(`Arjun Specialization: "${spec}"`);

        // 2. Get Patients and their visited status
        const [pats] = await conn.query(
            "SELECT name, is_visited, department FROM patients WHERE is_active = 1 AND department = ?",
            [spec]
        );
        
        console.log('--- PATIENTS IN ARJUN DEPT ---');
        console.table(pats);

    } catch (err) {
        console.error(err);
    } finally {
        if (conn) await conn.end();
    }
}
run();
