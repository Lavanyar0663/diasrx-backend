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

async function updateMobile() {
    console.log('--- Updating Pharmacist Mobile Number ---');
    
    try {
        // 1. Get correct user_id for ravi@diasrx.com
        const [users] = await pool.query("SELECT id FROM users WHERE email = 'ravi@diasrx.com'");
        if (users.length === 0) {
            console.error('  ERROR: User ravi@diasrx.com not found in users table!');
            process.exit(1);
        }
        const correctUserId = users[0].id;
        console.log(`  Found correct user_id: ${correctUserId}`);

        // 2. Sync user_id and update phone in pharmacists table
        const updateSql = `
            UPDATE pharmacists 
            SET user_id = ?, 
                phone = '9988776655', 
                professional_title = 'Senior Pharmacist',
                specialization = 'Clinical Pharmacy'
            WHERE email = 'ravi@diasrx.com'
        `;
        const [result] = await pool.query(updateSql, [correctUserId]);
        
        if (result.matchedRows === 0) {
            console.log('  WARNING: No record found for ravi@diasrx.com in pharmacists table. Creating one...');
            const insertSql = `
                INSERT INTO pharmacists (user_id, name, email, phone, professional_title, specialization)
                VALUES (?, 'Ph. Ravi Kumar', 'ravi@diasrx.com', '9988776655', 'Senior Pharmacist', 'Clinical Pharmacy')
            `;
            await pool.query(insertSql, [correctUserId]);
            console.log('  SUCCESS: New pharmacist record created and mapped.');
        } else {
            console.log(`  SUCCESS: Updated mobile and synced mapping for Ravi Kumar (Rows matched: ${result.matchedRows}).`);
        }

        console.log('--- Update Complete! ---');
    } catch (err) {
        console.error('  CRITICAL ERROR:', err.message);
    } finally {
        process.exit(0);
    }
}

updateMobile();
