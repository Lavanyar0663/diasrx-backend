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

        const [users] = await conn.query('SELECT id, email, name FROM users WHERE email = ?', ['arjun.diasrx.com']);
        console.log('--- USER ---');
        console.log(users);

        if (users.length > 0) {
            const [docs] = await conn.query('SELECT id, specialization FROM doctors WHERE user_id = ?', [users[0].id]);
            console.log('--- DOCTOR ---');
            console.log(docs);
            
            const [allDocs] = await conn.query('SELECT id, user_id, name, specialization FROM doctors LIMIT 5');
            console.log('--- ALL DOCTORS SAMPLE ---');
            console.table(allDocs);
        }

        await conn.end();
    } catch (e) {
        console.error(e);
    }
}
run();
