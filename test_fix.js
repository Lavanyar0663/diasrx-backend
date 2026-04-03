const db = require("./db");
const { withTransaction } = require("./utils/dbHelper");

// Mocking some of the adminController logic for testing
async function testApprove(userId) {
    console.log(`[TEST] Starting approval test for userId ${userId}`);
    
    try {
        await withTransaction(async (connection) => {
            const [users] = await connection.query("SELECT * FROM users WHERE id = ?", [userId]);
            if (users.length === 0) throw new Error("User not found");
            const user = users[0];

            console.log(`[TEST] User found: ${user.name} (${user.role})`);

            // 1. Update users
            await connection.query(
                "UPDATE users SET status = 'APPROVED', is_active = 1, password = NULL WHERE id = ?",
                [userId]
            );
            console.log("[TEST] users table updated.");

            // 2. Sync to role-specific table
            if (user.role === 'doctor') {
                await connection.query(
                    "UPDATE doctors SET password = NULL, is_active = 1 WHERE user_id = ?",
                    [userId]
                );
                console.log("[TEST] doctors table updated.");
            } else if (user.role === 'pharmacist') {
                await connection.query(
                    "UPDATE pharmacists SET password = NULL, is_active = 1 WHERE user_id = ?",
                    [userId]
                );
                console.log("[TEST] pharmacists table updated.");
            }
        });

        console.log(`[TEST] SUCCESS: Approved userId ${userId}`);
    } catch (err) {
        console.error(`[TEST] FAILED: ${err.message}`);
    } finally {
        process.exit();
    }
}

testApprove(235);
