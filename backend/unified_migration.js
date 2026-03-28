const db = require("./db");

const migrations = [
    // Step 1: Create users table
    `CREATE TABLE IF NOT EXISTS users (
      id int NOT NULL AUTO_INCREMENT,
      name varchar(100) DEFAULT NULL,
      email varchar(100) NOT NULL,
      password varchar(255) DEFAULT NULL,
      role ENUM('admin', 'doctor', 'pharmacist', 'patient') NOT NULL,
      is_active BOOLEAN DEFAULT TRUE,
      created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      UNIQUE KEY email (email),
      INDEX idx_users_email (email)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;`,

    // Step 2: Add user_id to existing tables
    `ALTER TABLE admin ADD COLUMN user_id int DEFAULT NULL`,
    `ALTER TABLE admin ADD CONSTRAINT fk_admin_user FOREIGN KEY (user_id) REFERENCES users (id)`,
    `ALTER TABLE doctors ADD COLUMN user_id int DEFAULT NULL`,
    `ALTER TABLE doctors ADD CONSTRAINT fk_doctors_user FOREIGN KEY (user_id) REFERENCES users (id)`,
    `ALTER TABLE pharmacists ADD COLUMN user_id int DEFAULT NULL`,
    `ALTER TABLE pharmacists ADD CONSTRAINT fk_pharmacists_user FOREIGN KEY (user_id) REFERENCES users (id)`,

    // Step 3: Prescription Status
    `UPDATE prescription SET status = 'CREATED' WHERE status = 'pending'`,
    `ALTER TABLE prescription MODIFY COLUMN status ENUM('CREATED', 'DISPENSED', 'CANCELLED') DEFAULT 'CREATED'`,

    // Step 4: Remarks and Quantity
    `ALTER TABLE prescription ADD COLUMN remarks TEXT DEFAULT NULL`,
    `ALTER TABLE prescription_drugs ADD COLUMN quantity INT DEFAULT 1`,

    // Step 5: Dispense Logs
    `CREATE TABLE IF NOT EXISTS dispense_logs (
        id INT AUTO_INCREMENT PRIMARY KEY,
        prescription_id INT NOT NULL,
        idempotency_key VARCHAR(100) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (prescription_id) REFERENCES prescription(id) ON DELETE CASCADE
    )`,
    `CREATE INDEX idx_idempotency ON dispense_logs (prescription_id, idempotency_key)`,

    // Step 6: Soft Delete and Indexes
    `ALTER TABLE patients ADD COLUMN is_active BOOLEAN DEFAULT TRUE`,
    `ALTER TABLE drug_master ADD COLUMN is_active BOOLEAN DEFAULT TRUE`,
    `ALTER TABLE patients ADD INDEX idx_patients_doctor (doctor_id)`,
    `ALTER TABLE prescription ADD INDEX idx_prescription_patient (patient_id)`,
    `ALTER TABLE prescription ADD INDEX idx_prescription_doctor (doctor_id)`,
    `ALTER TABLE prescription ADD INDEX idx_prescription_patient_doctor_combo (patient_id, doctor_id)`,

    // Step 7: OTP for Forgot Password
    `ALTER TABLE users ADD COLUMN otp VARCHAR(6) DEFAULT NULL`,
    `ALTER TABLE users ADD COLUMN otp_expiry TIMESTAMP NULL DEFAULT NULL`
];

async function runMigrations() {
    console.log("Running migrations...");
    for (const sql of migrations) {
        try {
            await new Promise((resolve, reject) => {
                db.query(sql, (err) => {
                    if (err) {
                        // Ignore "duplicate column" or "duplicate index" or "duplicate key" errors
                        if (err.code === 'ER_DUP_FIELDNAME' || err.code === 'ER_DUP_KEYNAME' || err.code === 'ER_TABLE_EXISTS_ERROR' || err.code === 'ER_FK_DUP_NAME' || err.code === 'ER_DUP_CONSTRAINT_NAME') {
                            console.log(`Skipping: ${sql.substring(0, 50)}... (Already exists)`);
                            resolve();
                        } else {
                            reject(err);
                        }
                    } else {
                        console.log(`Executed: ${sql.substring(0, 50)}...`);
                        resolve();
                    }
                });
            });
        } catch (err) {
            console.error(`Error executing migration: ${sql}`);
            console.error(err);
        }
    }
    console.log("Migrations finished!");
    process.exit(0);
}

runMigrations();
