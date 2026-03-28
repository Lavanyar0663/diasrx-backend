const mysql = require('mysql2');
require('dotenv').config();

const connection = mysql.createConnection({
    host: process.env.DB_HOST || '127.0.0.1',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '',
    port: process.env.DB_PORT || 3306
});

connection.connect((err) => {
    if (err) {
        console.error('Error connecting:', err);
        process.exit(1);
    }
    console.log('Connected to MySQL server.');

    connection.query('SHOW DATABASES', (err, results) => {
        if (err) {
            console.error('Error listing databases:', err);
            process.exit(1);
        }
        console.log('Databases:', results.map(r => r.Database));
        
        const dbExists = results.some(r => r.Database.toLowerCase() === process.env.DB_NAME.toLowerCase());
        console.log(`Database '${process.env.DB_NAME}' exists: ${dbExists}`);
        
        connection.end();
    });
});
