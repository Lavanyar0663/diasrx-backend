const mysql = require('mysql2');
const bcrypt = require('bcrypt');
require('dotenv').config();

const pool = mysql.createPool({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    port: process.env.DB_PORT,
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
});

async function query(sql) {
    return new Promise((resolve, reject) => {
        pool.query(sql, (err, result) => {
            if (err) {
                // Ignore already exists errors
                if (['ER_TABLE_EXISTS_ERROR', 'ER_DUP_FIELDNAME', 'ER_DUP_KEYNAME', 'ER_FK_DUP_NAME'].includes(err.code)) {
                    console.log(`  SKIP (already exists): ${sql.substring(0, 60)}...`);
                    resolve();
                } else {
                    console.error(`  ERROR: ${err.message}`);
                    resolve(); // continue even on error
                }
            } else {
                console.log(`  OK: ${sql.substring(0, 60)}...`);
                resolve(result);
            }
        });
    });
}

async function setup() {
    console.log('=== DIAS Rx Full Database Setup ===\n');

    // 1. Create all tables
    await query(`CREATE TABLE IF NOT EXISTS users (
        id int NOT NULL AUTO_INCREMENT,
        name varchar(100) DEFAULT NULL,
        email varchar(100) NOT NULL,
        password varchar(255) DEFAULT NULL,
        role ENUM('admin', 'doctor', 'pharmacist', 'patient') NOT NULL,
        is_active BOOLEAN DEFAULT TRUE,
        otp VARCHAR(6) DEFAULT NULL,
        otp_expiry TIMESTAMP NULL DEFAULT NULL,
        created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        PRIMARY KEY (id),
        UNIQUE KEY email (email)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci`);

    await query(`CREATE TABLE IF NOT EXISTS admin (
        id int NOT NULL AUTO_INCREMENT,
        name varchar(100) DEFAULT NULL,
        email varchar(100) NOT NULL,
        password varchar(255) DEFAULT NULL,
        user_id int DEFAULT NULL,
        PRIMARY KEY (id),
        UNIQUE KEY email (email)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`);

    await query(`CREATE TABLE IF NOT EXISTS doctors (
        id int NOT NULL AUTO_INCREMENT,
        name varchar(100) DEFAULT NULL,
        email varchar(100) NOT NULL,
        password varchar(255) DEFAULT NULL,
        specialization varchar(100) DEFAULT NULL,
        phone varchar(20) DEFAULT NULL,
        professional_title varchar(100) DEFAULT NULL,
        avatar_url varchar(255) DEFAULT NULL,
        user_id int DEFAULT NULL,
        PRIMARY KEY (id),
        UNIQUE KEY email (email)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`);

    await query(`CREATE TABLE IF NOT EXISTS pharmacists (
        id int NOT NULL AUTO_INCREMENT,
        name varchar(100) DEFAULT NULL,
        email varchar(100) NOT NULL,
        password varchar(255) DEFAULT NULL,
        user_id int DEFAULT NULL,
        PRIMARY KEY (id),
        UNIQUE KEY email (email)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`);

    await query(`CREATE TABLE IF NOT EXISTS patients (
        id int NOT NULL AUTO_INCREMENT,
        name varchar(100) DEFAULT NULL,
        email varchar(100) DEFAULT NULL,
        age int DEFAULT NULL,
        gender varchar(10) DEFAULT NULL,
        doctor_id int DEFAULT NULL,
        is_active BOOLEAN DEFAULT TRUE,
        PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`);

    await query(`CREATE TABLE IF NOT EXISTS drug_master (
        id int NOT NULL AUTO_INCREMENT,
        name varchar(200) DEFAULT NULL,
        is_active BOOLEAN DEFAULT TRUE,
        PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`);

    await query(`CREATE TABLE IF NOT EXISTS prescription (
        id int NOT NULL AUTO_INCREMENT,
        patient_id int DEFAULT NULL,
        doctor_id int DEFAULT NULL,
        diagnosis TEXT DEFAULT NULL,
        remarks TEXT DEFAULT NULL,
        status ENUM('CREATED', 'DISPENSED', 'CANCELLED') DEFAULT 'CREATED',
        created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`);

    await query(`CREATE TABLE IF NOT EXISTS prescription_drugs (
        id int NOT NULL AUTO_INCREMENT,
        prescription_id int DEFAULT NULL,
        drug_id int DEFAULT NULL,
        dosage varchar(100) DEFAULT NULL,
        frequency varchar(100) DEFAULT NULL,
        duration varchar(100) DEFAULT NULL,
        quantity INT DEFAULT 1,
        PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`);

    await query(`CREATE TABLE IF NOT EXISTS dispense_logs (
        id INT AUTO_INCREMENT PRIMARY KEY,
        prescription_id INT NOT NULL,
        idempotency_key VARCHAR(100) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`);

    console.log('\n=== Seeding Test Users ===\n');

    const hash = await bcrypt.hash('password123', 10);

    // Seed drug_master
    const drugs = [
        'Amoxicillin 500mg', 'Ibuprofen 400mg', 'Paracetamol 500mg', 'Azithromycin 250mg',
        'Ciprofloxacin 500mg', 'Metronidazole 400mg', 'Diclofenac 50mg', 'Omeprazole 20mg',
        'Cetirizine 10mg', 'Aspirin 75mg', 'Lidocaine 2% Injection', 'Chlorhexidine Mouthwash',
        'Clindamycin 300mg', 'Dexamethasone 4mg', 'Ketorolac 10mg'
    ];
    for (const drug of drugs) {
        await query(`INSERT IGNORE INTO drug_master (name, is_active) VALUES ('${drug}', 1)`);
    }

    // Users data array
    const seedUsers = [
        // Admin (1)
        { name: 'Admin Super', email: 'admin@diasrx.com', role: 'admin' },
        // Doctors (5)
        { name: 'Dr. Arjun Raman', email: 'arjun@diasrx.com', role: 'doctor', spec: 'General Dentistry', phone: '555-0123-456', title: 'Senior Orthodontist' },
        { name: 'Dr. Sarah Jenkins', email: 'sarah@diasrx.com', role: 'doctor', spec: 'Orthodontics', phone: '555-0987-654', title: 'Consultant Orthodontist' },
        { name: 'Dr. Michael Chen', email: 'michael@diasrx.com', role: 'doctor', spec: 'Periodontics', phone: '555-1111-222', title: 'Specialist Periodontist' },
        { name: 'Dr. Alana Smith', email: 'alana@diasrx.com', role: 'doctor', spec: 'Endodontics', phone: '555-3333-444', title: 'Endodontic Specialist' },
        { name: 'Dr. Robert Wilson', email: 'robert@diasrx.com', role: 'doctor', spec: 'Oral Surgery', phone: '555-5555-666', title: 'Oral & Maxillofacial Surgeon' },
        // Pharmacists (3)
        { name: 'Ph. Ravi Kumar', email: 'ravi@diasrx.com', role: 'pharmacist' },
        { name: 'Ph. John Doe', email: 'john@diasrx.com', role: 'pharmacist' },
        { name: 'Ph. Emily Watson', email: 'emily@diasrx.com', role: 'pharmacist' },
        // Patients (15)
        // Patients (15) - MIXED EMAILS for realistic simulation
        { name: 'Meena Krishnan', email: 'meena@diasrx.com', role: 'patient', age: 34, gender: 'Female' }, // With Email
        { name: 'Michael Ross', email: null, role: 'patient', age: 45, gender: 'Male' },            // NO EMAIL
        { name: 'David Miller', email: 'david@diasrx.com', role: 'patient', age: 28, gender: 'Male' },   // With Email
        { name: 'Emma Watson', email: null, role: 'patient', age: 22, gender: 'Female' },          // NO EMAIL
        { name: 'Robert Chen', email: 'robert.c@diasrx.com', role: 'patient', age: 51, gender: 'Male' }, // With Email
        { name: 'Sophia Lee', email: null, role: 'patient', age: 30, gender: 'Female' },           // NO EMAIL
        { name: 'James Taylor', email: 'james@diasrx.com', role: 'patient', age: 60, gender: 'Male' },   // With Email
        { name: 'Olivia Brown', email: null, role: 'patient', age: 25, gender: 'Female' },         // NO EMAIL
        { name: 'William Davis', email: 'william@diasrx.com', role: 'patient', age: 40, gender: 'Male' },// With Email
        { name: 'Mia Wilson', email: null, role: 'patient', age: 33, gender: 'Female' },           // NO EMAIL
        { name: 'Benjamin Moore', email: 'benjamin@diasrx.com', role: 'patient', age: 55, gender: 'Male' },// With Email
        { name: 'Isabella Clark', email: null, role: 'patient', age: 27, gender: 'Female' },        // NO EMAIL
        { name: 'Lucas Lewis', email: 'lucas@diasrx.com', role: 'patient', age: 38, gender: 'Male' },   // With Email
        { name: 'Amelia Walker', email: 'amelia@diasrx.com', role: 'patient', age: 42, gender: 'Female' },// With Email
        { name: 'Henry Hall', email: null, role: 'patient', age: 65, gender: 'Male' }              // NO EMAIL
    ];

    for (const u of seedUsers) {
        let userId = null;
        if (u.email) {
            await query(`INSERT INTO users (name, email, password, role, is_active) VALUES ('${u.name}', '${u.email}', '${hash}', '${u.role}', 1) ON DUPLICATE KEY UPDATE is_active=1, password='${hash}'`);
            let rows = await query(`SELECT id FROM users WHERE email='${u.email}'`);
            if (rows && rows.length > 0) userId = rows[0].id;
        }
        
        if (u.role === 'admin') {
            await query(`INSERT IGNORE INTO admin (name, email, password, user_id) VALUES ('${u.name}', '${u.email}', '${hash}', ${userId})`);
        } else if (u.role === 'doctor') {
            await query(`INSERT IGNORE INTO doctors (name, email, password, specialization, phone, professional_title, user_id) 
                         VALUES ('${u.name}', '${u.email}', '${hash}', '${u.spec || ''}', '${u.phone || ''}', '${u.title || ''}', ${userId})`);
        } else if (u.role === 'pharmacist') {
            await query(`INSERT IGNORE INTO pharmacists (name, email, password, user_id) VALUES ('${u.name}', '${u.email}', '${hash}', ${userId})`);
        } else if (u.role === 'patient') {
            // Patient email can be null
            const emailValue = u.email ? `'${u.email}'` : 'NULL';
            await query(`INSERT IGNORE INTO patients (name, email, age, gender) VALUES ('${u.name}', ${emailValue}, ${u.age || 30}, '${u.gender || 'Unknown'}')`);
        }
    }

    console.log('\n=== Setup Complete! ===');
    console.log('Test credentials (all passwords: password123):');
    console.log('  Admin:       admin@diasrx.com');
    console.log('  Doctor:      arjun@diasrx.com');
    console.log('  Pharmacist:  ravi@diasrx.com');
    console.log('  Patient:     meena@diasrx.com');
    pool.end();
}

setup();
