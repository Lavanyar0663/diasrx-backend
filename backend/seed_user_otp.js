const mysql = require('mysql2');
const bcrypt = require('bcrypt');
require('dotenv').config();

async function seed() {
    const ports = [3307, 3306];
    const email = 'meena.krishnan@diasrx.com';
    const password = 'password123';
    const hashedPassword = await bcrypt.hash(password, 10);

    for (const port of ports) {
        console.log(`Trying to seed on port ${port}...`);
        const connection = mysql.createConnection({
            host: 'localhost',
            user: process.env.DB_USER || 'root',
            password: process.env.DB_PASSWORD || '',
            database: process.env.DB_NAME || 'diasrx',
            port: port
        });

        try {
            await connection.promise().query(
                "INSERT INTO users (name, email, password, role, is_active) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE is_active=1",
                ['Meena Krishnan', email, hashedPassword, 'patient', 1]
            );
            console.log(`\nSUCCESS: Seeded user ${email} on port ${port}`);
            connection.end();
            return;
        } catch (err) {
            console.log(`Failed on port ${port}: ${err.message}`);
            connection.destroy();
        }
    }
}

seed();
