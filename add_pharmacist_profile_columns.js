const mysql = require('mysql2');
require('dotenv').config({ path: './backend/.env' });

const pool = mysql.createPool({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    port: process.env.DB_PORT,
    waitForConnections: true,
    connectionLimit: 1,
    queueLimit: 0
}).promise();

async function migrate() {
    console.log('--- Migrating Pharmacists Table ---');
    
    const queries = [
        "ALTER TABLE pharmacists ADD COLUMN professional_title VARCHAR(100) DEFAULT 'Pharmacist'",
        "ALTER TABLE pharmacists ADD COLUMN specialization VARCHAR(100) DEFAULT 'Pharmacy Dept.'",
        "ALTER TABLE pharmacists ADD COLUMN phone VARCHAR(20) DEFAULT NULL",
        "UPDATE pharmacists SET professional_title = 'Senior Pharmacist', specialization = 'Clinical Pharmacy', phone = '555-4420-111' WHERE email = 'ravi@diasrx.com'"
    ];

    for (const q of queries) {
        try {
            await pool.query(q);
            console.log('  SUCCESS: ' + q.substring(0, 50) + '...');
        } catch (err) {
            if (err.code === 'ER_DUP_COLUMNNAME') {
                console.log('  SKIP (column already exists): ' + q.substring(0, 50) + '...');
            } else {
                console.error('  ERROR during query [' + q + ']: ', err.message);
            }
        }
    }

    console.log('--- Migration Complete! ---');
    process.exit(0);
}

migrate();
