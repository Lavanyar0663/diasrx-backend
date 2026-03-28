const db = require("./db");

const migrationQueries = [
    // users table
    "ALTER TABLE users MODIFY name VARCHAR(100) NOT NULL",
    "ALTER TABLE users MODIFY phone VARCHAR(20) NOT NULL",
    "ALTER TABLE users MODIFY password VARCHAR(255) NOT NULL",
    "ALTER TABLE users MODIFY role ENUM('admin', 'doctor', 'pharmacist', 'patient') NOT NULL",
    "ALTER TABLE users MODIFY status ENUM('PENDING', 'APPROVED', 'REJECTED', 'INITIATED', 'CREATED') NOT NULL DEFAULT 'CREATED'",
    "ALTER TABLE users MODIFY gender VARCHAR(15) NOT NULL DEFAULT 'Not Specified'",
    "ALTER TABLE users MODIFY age INT NOT NULL DEFAULT 0",
    "ALTER TABLE users ADD UNIQUE (phone)",
    "ALTER TABLE users ADD UNIQUE (email)",

    // doctors table
    "ALTER TABLE doctors MODIFY name VARCHAR(100) NOT NULL",
    "ALTER TABLE doctors MODIFY email VARCHAR(100) NOT NULL",
    "ALTER TABLE doctors MODIFY specialization VARCHAR(100) NOT NULL",
    "ALTER TABLE doctors MODIFY phone VARCHAR(20) NOT NULL",
    "ALTER TABLE doctors MODIFY user_id INT NOT NULL",
    "ALTER TABLE doctors ADD UNIQUE (phone)",
    "ALTER TABLE doctors ADD UNIQUE (email)",

    // patients table
    "ALTER TABLE patients MODIFY name VARCHAR(100) NOT NULL",
    "ALTER TABLE patients MODIFY phone VARCHAR(20) NOT NULL",
    "ALTER TABLE patients MODIFY doctor_id INT NOT NULL",
    "ALTER TABLE patients MODIFY department VARCHAR(100) NOT NULL",
    "ALTER TABLE patients MODIFY gender VARCHAR(10) NOT NULL DEFAULT 'Not Specified'",
    "ALTER TABLE patients MODIFY age INT NOT NULL DEFAULT 0",
    "ALTER TABLE patients ADD COLUMN IF NOT EXISTS user_id INT",

    // prescription table
    "ALTER TABLE prescription MODIFY patient_id INT NOT NULL",
    "ALTER TABLE prescription MODIFY doctor_id INT NOT NULL",
    "ALTER TABLE prescription MODIFY diagnosis TEXT NOT NULL",
    "ALTER TABLE prescription MODIFY status ENUM('CREATED','DISPENSED','CANCELLED') NOT NULL DEFAULT 'CREATED'",

    // prescription_drugs table
    "ALTER TABLE prescription_drugs MODIFY prescription_id INT NOT NULL",
    "ALTER TABLE prescription_drugs MODIFY drug_id INT NOT NULL",
    "ALTER TABLE prescription_drugs MODIFY dosage VARCHAR(100) NOT NULL",
    "ALTER TABLE prescription_drugs MODIFY frequency VARCHAR(100) NOT NULL",
    "ALTER TABLE prescription_drugs MODIFY duration VARCHAR(100) NOT NULL",
    "ALTER TABLE prescription_drugs MODIFY quantity INT NOT NULL DEFAULT 1"
];

async function runMigration() {
    console.log("Starting database migration...");
    for (const query of migrationQueries) {
        try {
            await new Promise((resolve, reject) => {
                db.query(query, (err) => err ? reject(err) : resolve());
            });
            console.log(`Successfully executed: ${query.substring(0, 50)}...`);
        } catch (err) {
            console.error(`Failed: ${query.substring(0, 50)}... Error: ${err.message}`);
        }
    }
    console.log("Migration complete!");
    process.exit(0);
}

runMigration();
