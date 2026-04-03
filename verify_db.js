const db = require('./db');

db.query('DESCRIBE users', (err, rows) => {
    if (err) {
        console.error("Error describing users table:", err);
        process.exit(1);
    }
    console.log("Users table structure:");
    console.table(rows);

    const hasOtp = rows.some(r => r.Field === 'otp');
    const hasExpiry = rows.some(r => r.Field === 'otp_expiry');

    if (hasOtp && hasExpiry) {
        console.log("OTP columns are present.");
    } else {
        console.log("OTP columns are MISSING.");
        // Try to add them if missing
        db.query("ALTER TABLE users ADD COLUMN otp VARCHAR(6) DEFAULT NULL, ADD COLUMN otp_expiry TIMESTAMP NULL DEFAULT NULL", (err2) => {
            if (err2) {
                console.error("Error adding columns:", err2);
            } else {
                console.log("OTP columns added successfully.");
            }
            process.exit(0);
        });
        return;
    }
    process.exit(0);
});
