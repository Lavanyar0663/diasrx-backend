const mysql = require('mysql2/promise');
const dotenv = require('dotenv');

dotenv.config();

async function migrate() {
    const connection = await mysql.createConnection({
        host: process.env.DB_HOST || '127.0.0.1',
        port: process.env.DB_PORT || 3307,
        user: process.env.DB_USER || 'root',
        password: process.env.DB_PASSWORD || '',
        database: process.env.DB_NAME || 'diasrx'
    });

    console.log('Connected to database for migration...');

    try {
        // Add avatar_url to users table
        const [usersCols] = await connection.query('SHOW COLUMNS FROM users LIKE "avatar_url"');
        if (usersCols.length === 0) {
            await connection.query('ALTER TABLE users ADD COLUMN avatar_url TEXT AFTER professional_title');
            console.log('Added avatar_url column to users table.');
        } else {
            console.log('avatar_url column already exists in users table.');
        }

        // Add specialization to pharmacists if it doesn't exist (optional, but good for consistency)
        const [pharmCols] = await connection.query('SHOW COLUMNS FROM pharmacists LIKE "department"');
        if (pharmCols.length === 0) {
            await connection.query('ALTER TABLE pharmacists ADD COLUMN department VARCHAR(255) AFTER phone');
            console.log('Added department column to pharmacists table.');
        }

        const [pharmCols2] = await connection.query('SHOW COLUMNS FROM pharmacists LIKE "professional_title"');
        if (pharmCols2.length === 0) {
            await connection.query('ALTER TABLE pharmacists ADD COLUMN professional_title VARCHAR(255) AFTER department');
            console.log('Added professional_title column to pharmacists table.');
        }

        const [pharmCols3] = await connection.query('SHOW COLUMNS FROM pharmacists LIKE "avatar_url"');
        if (pharmCols3.length === 0) {
            await connection.query('ALTER TABLE pharmacists ADD COLUMN avatar_url TEXT AFTER professional_title');
            console.log('Added avatar_url column to pharmacists table.');
        }

    } catch (err) {
        console.error('Migration failed:', err);
    } finally {
        await connection.end();
        console.log('Migration finished.');
    }
}

migrate();
