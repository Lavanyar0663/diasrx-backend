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

        console.log('--- Searching for Arjun ---');
        const [users] = await conn.query(
            "SELECT id, name, email, role FROM users WHERE email LIKE ? OR name LIKE ?",
            ['%arjun%', '%Arjun%']
        );
        console.log('Users found:', users);

        if (users.length > 0) {
            for (const user of users) {
                console.log(`\n--- Doctor Profile for User ID ${user.id} (${user.name}) ---`);
                const [docs] = await conn.query("SELECT id, name, specialization FROM doctors WHERE user_id = ?", [user.id]);
                console.log('Doctor rows:', docs);

                if (docs.length > 0) {
                    const doc = docs[0];
                    console.log(`\n--- Patients for Doctor ID ${doc.id} or Dept ${doc.specialization} ---`);
                    const [pats] = await conn.query(
                        "SELECT id, name, department, doctor_id FROM patients WHERE doctor_id = ? OR (doctor_id IS NULL AND department = ?)",
                        [doc.id, doc.specialization]
                    );
                    console.log(`Total patients found: ${pats.length}`);
                    if (pats.length > 0) {
                        console.table(pats.slice(0, 10)); // Show first 10
                    }
                }
            }
        }

        console.log('\n--- Sample Patients from DB ---');
        const [allPats] = await conn.query("SELECT id, name, department, doctor_id FROM patients LIMIT 5");
        console.table(allPats);

        await conn.end();
    } catch (err) {
        console.error('Error:', err);
    }
}
run();
