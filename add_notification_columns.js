const mysql = require('mysql2');
const dotenv = require('dotenv');
dotenv.config();

const connection = mysql.createConnection({
    host: process.env.DB_HOST || '127.0.0.1',
    port: process.env.DB_PORT || 3307,
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '',
    database: process.env.DB_NAME || 'diasrx'
});

connection.connect((err) => {
    if (err) {
        console.error('Error connecting to DB:', err);
        return;
    }
    console.log('Connected to DB');

    const sql = `
    ALTER TABLE users 
    ADD COLUMN isWhatsAppEnabled TINYINT(1) DEFAULT 1,
    ADD COLUMN isEmailEnabled TINYINT(1) DEFAULT 1,
    ADD COLUMN isLatencyEnabled TINYINT(1) DEFAULT 1,
    ADD COLUMN isOnboardingEnabled TINYINT(1) DEFAULT 1
    `;

    connection.query(sql, (err, results) => {
        if (err) {
            console.error('Error adding columns:', err);
        } else {
            console.log('Successfully added notification columns');
        }
        connection.end();
    });
});
