const db = require("./db");
const bcrypt = require("bcrypt");

async function resetDoctorPasswords() {
    try {
        const hashedPassword = await bcrypt.hash("password123", 10);
        
        await new Promise((resolve, reject) => {
            db.query("UPDATE users SET password = ? WHERE role = 'doctor'", [hashedPassword], (err, result) => {
                if (err) return reject(err);
                console.log(`Successfully reset passwords for ${result.affectedRows} doctors.`);
                resolve(result);
            });
        });
    } catch (err) {
        console.error("Error resetting passwords", err);
    } finally {
        process.exit(0);
    }
}

resetDoctorPasswords();
