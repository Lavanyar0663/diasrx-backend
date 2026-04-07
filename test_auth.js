const db = require('./db');

async function test() {
    const prescId = 66;
    const userId = 7; // Dr. Sarah Jenkins' users.id

    console.log('--- Testing isAuthorized for getExplanation ---');
    // documentController.js logic:
    // const isAuthorized = (prescription, user) => {
    //   if (user.role === "doctor") return String(prescription.doctor_id) === String(user.id);
    
    db.query('SELECT doctor_id FROM prescription WHERE id = ?', [prescId], (err, rows) => {
        if (rows.length > 0) {
            const docIdInPresc = rows[0].doctor_id; // 2
            console.log('Doctor ID in prescription:', docIdInPresc);
            console.log('User ID in token:', userId);
            console.log('Is Authorized?', String(docIdInPresc) === String(userId));
        }
        db.end();
    });
}

test();
