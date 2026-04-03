const mysql = require('mysql2/promise');
require('dotenv').config({ path: './backend/.env' });

async function run() {
    try {
        const conn = await mysql.createConnection({
            host: process.env.DB_HOST,
            user: process.env.DB_USER,
            password: process.env.DB_PASSWORD,
            database: process.env.DB_NAME,
            port: process.env.DB_PORT
        });

        const [users] = await conn.query('SELECT id, email, name, role FROM users WHERE email LIKE ?', ['%arjun%']);
        console.log('--- USERS ---');
        console.log(JSON.stringify(users, null, 2));

        if (users.length > 0) {
            const [docs] = await conn.query('SELECT * FROM doctors WHERE user_id = ?', [users[0].id]);
            console.log('--- DOCTOR PROFILE ---');
            console.log(JSON.stringify(docs, null, 2));

            if (docs.length > 0) {
                const docId = docs[0].id;
                const spec = docs[0].specialization;
                console.log(`Doctor ID: ${docId}, Specialization: "${spec}"`);

                const [pats] = await conn.query(
                    'SELECT id, name, department, doctor_id FROM patients WHERE doctor_id = ? OR (doctor_id IS NULL AND department = ?)',
                    [docId, spec]
                );
                console.log('--- MATCHING PATIENTS ---');
                console.table(pats);
            }
        }

        const [allPats] = await conn.query('SELECT id, name, department, doctor_id FROM patients LIMIT 10');
        console.log('--- ALL PATIENTS SAMPLE ---');
        console.table(allPats);

        await conn.end();
    } catch (e) {
        console.error(e);
    }
}
run();
