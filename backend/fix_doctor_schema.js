const mysql = require('mysql2');
require('dotenv').config();

const pool = mysql.createPool({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    port: process.env.DB_PORT
});

async function query(sql) {
    return new Promise((resolve, reject) => {
        pool.query(sql, (err, result) => {
            if (err) {
                if (err.code === 'ER_DUP_FIELDNAME') {
                    console.log(`  SKIP: ${sql.substring(0, 50)}... (Already exists)`);
                    resolve();
                } else {
                    reject(err);
                }
            } else {
                console.log(`  OK: ${sql.substring(0, 50)}...`);
                resolve(result);
            }
        });
    });
}

async function migrate() {
    console.log('--- Migrating Doctors Table ---');
    try {
        await query("ALTER TABLE doctors ADD COLUMN IF NOT EXISTS phone VARCHAR(20) AFTER specialization");
        await query("ALTER TABLE doctors ADD COLUMN IF NOT EXISTS professional_title VARCHAR(100) AFTER phone");
        await query("ALTER TABLE doctors ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(255) AFTER professional_title");
        console.log('--- Migration Complete ---');
    } catch (err) {
        console.error('Migration failed:', err.message);
    } finally {
        pool.end();
    }
}

migrate();
