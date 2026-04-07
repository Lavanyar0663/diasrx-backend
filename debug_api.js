const db = require('./db');

async function test() {
    const patientId = 89;
    const userId = 7; // Dr. Sarah Jenkins' users.id
    const doctorId = 2; // Dr. Sarah Jenkins' doctors.id

    console.log('--- Testing getPrescriptionsByPatient (Doctor view) ---');
    // Backend logic: if (role === "doctor") sql += " AND p.doctor_id = ?"; params.push(userId);
    const sqlDoctor = `
        SELECT p.*, d.name as doctor_name, d.specialization as doctor_department, DATE_FORMAT(p.created_at, '%d/%m/%Y, %H:%i') as formatted_date
        FROM prescription p
        JOIN doctors d ON p.doctor_id = d.id
        WHERE p.patient_id = ? AND p.doctor_id = ?`;
    
    db.query(sqlDoctor, [patientId, userId], (err, rows) => {
        if (err) console.error('Error:', err);
        console.log('getPrescriptionsByPatient (with userId 7):', rows.length, 'rows found');
    });

    console.log('\n--- Testing getPrescriptionsByPatient (Admin view) ---');
    const sqlAdmin = `
        SELECT p.*, d.name as doctor_name, d.specialization as doctor_department, DATE_FORMAT(p.created_at, '%d/%m/%Y, %H:%i') as formatted_date
        FROM prescription p
        JOIN doctors d ON p.doctor_id = d.id
        WHERE p.patient_id = ?`;
    
    db.query(sqlAdmin, [patientId], (err, rows) => {
        if (err) console.error('Error:', err);
        console.log('getPrescriptionsByPatient (Admin view):', rows.length, 'rows found');
        if (rows.length > 0) {
            console.log('Sample Prescription ID:', rows[0].id);
            // Check drugs
            db.query('SELECT pd.*, dm.name as drug_name FROM prescription_drugs pd JOIN drug_master dm ON pd.drug_id = dm.id WHERE pd.prescription_id = ?', [rows[0].id], (err2, drugs) => {
                console.log('Drugs found:', drugs.map(d => d.drug_name));
            });
        }
    });

    // Wait a bit and close
    setTimeout(() => db.end(), 2000);
}

test();
