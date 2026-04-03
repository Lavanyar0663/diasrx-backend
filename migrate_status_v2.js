const db = require("./db");

async function migrate() {
    try {
        console.log("Starting status enum migration...");

        // 1. Alter table to include ACTIVE
        const alterSql = "ALTER TABLE users MODIFY COLUMN status ENUM('PENDING','APPROVED','ACTIVE','REJECTED','INITIATED','CREATED') DEFAULT 'PENDING'";
        await db.promise().query(alterSql);
        console.log("Updated status ENUM to include 'ACTIVE'.");

        // 2. Sync legacy APPROVED to ACTIVE
        const [res1] = await db.promise().query("UPDATE users SET status = 'ACTIVE' WHERE status = 'APPROVED' OR status = ''");
        console.log(`Updated ${res1.affectedRows} users to 'ACTIVE' status.`);

        // 3. Ensure admin@diasrx.com is ACTIVE
        const [res2] = await db.promise().query("UPDATE users SET status = 'ACTIVE', is_active = 1 WHERE email = 'admin@diasrx.com'");
        console.log(`Forced 'ACTIVE' status for ${res2.affectedRows} admin account(s).`);

        // 4. Sync CREATED to PENDING
        const [res3] = await db.promise().query("UPDATE users SET status = 'PENDING' WHERE status = 'CREATED'");
        console.log(`Updated ${res3.affectedRows} users from 'CREATED' to 'PENDING'.`);

        // 5. Final sync is_active for all ACTIVE users
        await db.promise().query("UPDATE users SET is_active = 1 WHERE status = 'ACTIVE'");
        console.log("Synchronized 'is_active' for all ACTIVE users.");

        console.log("Migration completed successfully.");
        process.exit(0);
    } catch (error) {
        console.error("Migration failed:", error.message);
        process.exit(1);
    }
}

migrate();
