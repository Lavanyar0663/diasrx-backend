const db = require("./db");

async function fixAdmin() {
    try {
        console.log("Fixing admin status...");
        const [res] = await db.promise().query(
            "UPDATE users SET status = 'ACTIVE', is_active = 1 WHERE email = 'admin@diasrx.com'"
        );
        console.log(`Updated ${res.affectedRows} admin records.`);
        
        const [check] = await db.promise().query(
            "SELECT email, status, is_active FROM users WHERE email = 'admin@diasrx.com'"
        );
        console.log("Current Admin Record:", check[0]);
        
        process.exit(0);
    } catch (error) {
        console.error("Failed to fix admin:", error.message);
        process.exit(1);
    }
}

fixAdmin();
