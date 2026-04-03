const db = require("./db");

async function fixFinal() {
    try {
        console.log("Audit - All Users:");
        const [users] = await db.promise().query("SELECT id, email, role, status FROM users");
        users.forEach(u => console.log(`- ID: ${u.id}, Email: ${u.email}, Role: ${u.role}, Status: ${u.status}`));

        console.log("\nSearching specifically for admin@diasrx.com...");
        const [admin] = await db.promise().query("SELECT * FROM users WHERE email = 'admin@diasrx.com'");
        if (admin.length > 0) {
            console.log("Found admin record:", JSON.stringify(admin[0], null, 2));
            
            // Fix it!
            console.log("Force activating admin account...");
            await db.promise().query(
                "UPDATE users SET role = 'admin', status = 'ACTIVE', is_active = 1 WHERE email = 'admin@diasrx.com'"
            );
            console.log("Admin account updated.");
        } else {
            console.warn("CRITICAL: admin@diasrx.com NOT found in users table.");
            console.log("Will check if any other user is an admin...");
            const [others] = await db.promise().query("SELECT * FROM users WHERE role = 'admin'");
            console.log("Other admins found:", others.length, others.map(o => o.email));
        }

        // Global Sync!
        console.log("\nSyncing all other legacy statuses...");
        await db.promise().query("UPDATE users SET status = 'ACTIVE' WHERE status = 'APPROVED'");
        await db.promise().query("UPDATE users SET status = 'PENDING' WHERE status = 'CREATED' OR status = 'INITIATED'");
        await db.promise().query("UPDATE users SET is_active = 1 WHERE status = 'ACTIVE'");

        console.log("Final cleanup completed.");
        process.exit(0);
    } catch (err) {
        console.error("Cleanup failed:", err.message);
        process.exit(1);
    }
}

fixFinal();
