const db = require('./db');

async function research() {
    try {
        console.log("--- Researching Dr. Sarah ---");
        const [users] = await db.promise().query("SELECT id, name, role, email FROM users WHERE name LIKE '%Sarah%'");
        console.log("Users:", JSON.stringify(users, null, 2));

        if (users.length > 0) {
            const userIds = users.map(u => u.id);
            const [doctors] = await db.promise().query("SELECT id, user_id, name, specialization FROM doctors WHERE user_id IN (?)", [userIds]);
            console.log("Doctors:", JSON.stringify(doctors, null, 2));
        }

        console.log("\n--- Researching Patient Kavin ---");
        const [patients] = await db.promise().query("SELECT id, name, pid, doctor_id FROM patients WHERE name LIKE '%Kavin%'");
        console.log("Patients:", JSON.stringify(patients, null, 2));

        if (patients.length > 0) {
            const patientIds = patients.map(p => p.id);
            const [prescriptions] = await db.promise().query("SELECT id, patient_id, doctor_id, diagnosis, status FROM prescription WHERE patient_id IN (?)", [patientIds]);
            console.log("Prescriptions:", JSON.stringify(prescriptions, null, 2));
        }

        process.exit(0);
    } catch (error) {
        console.error("Research failed:", error);
        process.exit(1);
    }
}

research();
