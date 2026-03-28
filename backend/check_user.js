const mysql = require('mysql2');
require('dotenv').config();

const ports = [3307, 3306, 3308];
const hosts = ['localhost', '127.0.0.1'];
const emailToFind = 'meena.krishnan@diasrx.com';

async function check() {
    for (const host of hosts) {
        for (const port of ports) {
            console.log(`Trying ${host}:${port}...`);
            const connection = mysql.createConnection({
                host: host,
                user: process.env.DB_USER || 'root',
                password: process.env.DB_PASSWORD || '',
                database: process.env.DB_NAME || 'diasrx',
                port: port
            });

            try {
                const [rows] = await connection.promise().query('SELECT name, email, role FROM users');
                console.log(`Connection successful on ${host}:${port}!`);
                console.log('Users in DB:');
                rows.forEach(r => console.log(`- ${r.name} (${r.email}) [${r.role}]`));
                
                const user = rows.find(r => r.email.toLowerCase() === emailToFind.toLowerCase());
                if (user) {
                    console.log(`\nSUCCESS: Found user ${emailToFind}`);
                } else {
                    console.log(`\nFAILURE: User ${emailToFind} NOT found in database.`);
                }
                connection.end();
                return;
            } catch (err) {
                console.log(`Failed on ${host}:${port}: ${err.message}`);
                connection.destroy();
            }
        }
    }
}

check();
