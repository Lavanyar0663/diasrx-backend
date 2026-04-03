const db = require("./db");

async function syncData() {
    try {
        console.log("Starting data synchronization...");

        // Update APPROVED -> ACTIVE
        const [res1] = await db.promise().query("UPDATE users SET status = 'ACTIVE' WHERE status = 'APPROVED'");
        console.log(`Updated ${res1.affectedRows} users from 'APPROVED' to 'ACTIVE'.`);

        // Update CREATED -> PENDING
        const [res2] = await db.promise().query("UPDATE users SET status = 'PENDING' WHERE status = 'CREATED'");
        console.log(`Updated ${res2.affectedRows} users from 'CREATED' to 'PENDING'.`);

        // Update is_active for all ACTIVE users
        const [res3] = await db.promise().query("UPDATE users SET is_active = 1 WHERE status = 'ACTIVE'");
        console.log(`Synchronized 'is_active' for ${res3.affectedRows} 'ACTIVE' users.`);

        console.log("Data synchronization completed successfully.");
        process.exit(0);
    } catch (error) {
        console.error("Data synchronization failed:", error.message);
        process.exit(1);
    }
}

syncData();
