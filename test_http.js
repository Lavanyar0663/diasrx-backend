const db = require('./db');
const authService = require('./services/authService');

async function testFull() {
    try {
        const adminUsers = await new Promise((resolve, reject) => {
            db.query("SELECT * FROM users WHERE role='admin' LIMIT 1", (err, rows) => {
                if (err) return reject(err); resolve(rows);
            });
        });

        if (adminUsers.length === 0) {
            console.log("No admins found in DB!!!");
            process.exit(1);
        }

        const admin = adminUsers[0];
        const token = authService.generateToken({
            id: admin.id,
            email: admin.email,
            role: admin.role
        });

        const payload = {
            name: "Dr. Lavan Test Native",
            password: "password123",
            role: "doctor",
            phone: "9876500004", // unique phone!
            department: "Periodontics"
        };

        const res = await fetch('http://localhost:5000/api/auth/register', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(payload)
        });
        
        const data = await res.json();
        console.log("HTTP SUCCESS:", res.status);
        console.log("Data:", data);

    } catch (err) {
        console.error("Error:", err);
    }
    process.exit(0);
}
testFull();
