const mysql = require('mysql2/promise');
require('dotenv').config();

async function run() {
    try {
        const conn = await mysql.createConnection({
            host: process.env.DB_HOST,
            user: process.env.DB_USER,
            password: process.env.DB_PASSWORD,
            database: process.env.DB_NAME,
            port: process.env.DB_PORT
        });

        // 1. Get Arjun's info
        const [users] = await conn.query("SELECT id, name, email FROM users WHERE email='arjun.diasrx.com' LIMIT 1");
        if (users.length === 0) {
            console.log('USER arjun.diasrx.com NOT FOUND');
        } else {
            const user = users[0];
            const [docs] = await conn.query("SELECT id, specialization FROM doctors WHERE user_id = ?", [user.id]);
            if (docs.length === 0) {
                console.log('DOCTOR PROFILE NOT FOUND for user', user.id);
            } else {
                const doc = docs[0];
                console.log(`DOCTOR: ID=${doc.id}, Name=${user.name}, Spec="${doc.specialization}"`);
                
                // 2. See how many patients match this EXACT specialization
                const [count] = await conn.query("SELECT COUNT(*) as c FROM patients WHERE department = ?", [doc.specialization]);
                console.log(`Number of patients with department "${doc.specialization}": ${count[0].c}`);
            }
        }

        // 3. See all unique departments and their counts
        const [stats] = await conn.query("SELECT department, COUNT(*) as count FROM patients GROUP BY department");
        console.log('PATIENT DEPARTMENT STATS:', stats);

        await conn.end();
    } catch (err) {
        console.error(err);
    }
}
run();
