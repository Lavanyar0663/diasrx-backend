const db = require('./db');

async function q(sql, params) {
    return new Promise((resolve, reject) => {
        db.query(sql, params || [], (err, r) => err ? reject(err) : resolve(r));
    });
}

async function cleanupPatients() {
    // 1. Delete patients with empty/null phone
    const r1 = await q("DELETE FROM patients WHERE phone = '' OR phone IS NULL");
    console.log('Deleted empty-phone patients:', r1.affectedRows);

    // 2. Delete orphaned patients (no user_id, no matching user)
    const r2 = await q("DELETE FROM patients WHERE user_id IS NULL");
    console.log('Deleted null-user_id patients:', r2.affectedRows);

    // 3. Add unique constraint on phone for patients
    try {
        await q("ALTER TABLE patients ADD UNIQUE (phone)");
        console.log('Added UNIQUE(phone) to patients');
    } catch(e) {
        console.log('patients.phone unique constraint already exists or skipped:', e.message);
    }

    // 4. Add unique constraint on phone for pharmacists
    try {
        await q("ALTER TABLE pharmacists ADD UNIQUE (phone)");
        console.log('Added UNIQUE(phone) to pharmacists');
    } catch(e) {
        console.log('pharmacists.phone unique constraint skipped:', e.message);
    }

    console.log('Cleanup done!');
    process.exit(0);
}

cleanupPatients().catch(e => { console.error(e); process.exit(1); });
