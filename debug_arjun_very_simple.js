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

        const [users] = await conn.query("SELECT id, name, email FROM users WHERE email LIKE '%arjun%'");
        console.log('Arjun User:', users);

        if (users.length > 0) {
            const [docs] = await conn.query("SELECT id, specialization FROM doctors WHERE user_id = ?", [users[0].id]);
            console.log('Arjun Doctor Profile:', docs);

            if (docs.length > 0) {
                const [count] = await conn.query(
                    "SELECT COUNT(*) as count FROM patients WHERE doctor_id = ? OR (doctor_id IS NULL AND department = ?)",
                    [docs[0].id, docs[0].specialization]
                );
                console.log('Patient Count:', count[0].count);
            }
        }

        const [distinctDepts] = await conn.query("SELECT DISTINCT department FROM patients");
        console.log('Distinct Patient Depts:', distinctDepts.map(d => d.department));

        await conn.end();
    } catch (err) {
        console.error(err);
    }
}
run();
