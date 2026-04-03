const db = require("./db");

async function syncUsers() {
    try {
        const roles = ['admin', 'doctors', 'pharmacists'];
        for (const role of roles) {
            db.query(`SELECT * FROM ${role}`, async (err, rows) => {
                if (err) {
                    console.error(`Error fetching from ${role}:`, err);
                    return;
                }
                console.log(`Found ${rows.length} records in ${role}`);
                for (const row of rows) {
                    const mappedRole = role === 'doctors' ? 'doctor' : (role === 'pharmacists' ? 'pharmacist' : 'admin');
                    db.query("INSERT IGNORE INTO users (name, email, password, role) VALUES (?, ?, ?, ?)",
                        [row.name, row.email, row.password, mappedRole], (err) => {
                            if (err) console.error("Error inserting user:", err);
                        });
                }
            });
        }
    } catch (error) {
        console.error("Sync error:", error);
    }
}

syncUsers();
