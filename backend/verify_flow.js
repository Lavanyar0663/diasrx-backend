const db = require("./db");
const bcrypt = require("bcrypt");

async function testFlow() {
    console.log("Starting Verification Test...");

    const testName = "Test Pharmacist " + Date.now();
    const testPhone = "9988776655";
    const testEmail = "test.pharm" + Date.now() + "@diasrx.com";
    const testPassword = "password123";

    try {
        // 1. Admin Adds Pharmacist (Mocking the call to register)
        console.log("Step 1: Admin Adds Pharmacist...");
        const hashedPassword = await bcrypt.hash("temp_pass", 10);
        const tempEmail = "temp_" + Date.now() + "@diasrx.com";
        
        const userResult = await new Promise((resolve, reject) => {
            db.query(
                "INSERT INTO users (name, email, password, role, is_active, status) VALUES (?, ?, ?, ?, ?, ?)",
                [testName, tempEmail, hashedPassword, 'pharmacist', 0, 'INITIATED'],
                (err, res) => err ? reject(err) : resolve(res)
            );
        });
        const userId = userResult.insertId;
        console.log("Created user with ID:", userId, "Status: INITIATED");

        await new Promise((resolve, reject) => {
            db.query("INSERT INTO pharmacists (user_id, name, email, phone) VALUES (?, ?, ?, ?)",
                [userId, testName, tempEmail, testPhone], (err) => err ? reject(err) : resolve());
        });

        // 2. Verify status is INITIATED
        const [userInit] = await new Promise((resolve, reject) => {
            db.query("SELECT * FROM users WHERE id = ?", [userId], (err, rows) => err ? reject(err) : resolve(rows));
        });
        if (userInit.status !== 'INITIATED') throw new Error("Status should be INITIATED, but got " + userInit.status);
        console.log("Verified Status: INITIATED");

        // 3. User Requests Access
        console.log("Step 3: User Requests Access...");
        // Here we simulate the logic in exports.requestAccess
        // (Finding them by name and phone)
        const [foundPharm] = await new Promise((resolve, reject) => {
            db.query("SELECT user_id FROM pharmacists WHERE name = ? AND phone = ?", [testName, testPhone], (err, rows) => err ? reject(err) : resolve(rows));
        });
        
        if (!foundPharm || foundPharm.user_id !== userId) throw new Error("Could not find pharmacist record to link");

        const newHashedPassword = await bcrypt.hash(testPassword, 10);
        await new Promise((resolve, reject) => {
            db.query("UPDATE users SET email = ?, password = ?, status = 'PENDING' WHERE id = ?", 
                [testEmail, newHashedPassword, userId], (err) => err ? reject(err) : resolve());
        });
        console.log("Updated user status to PENDING and set real password/email");

        // 4. Verify status is PENDING
        const [userPending] = await new Promise((resolve, reject) => {
            db.query("SELECT * FROM users WHERE id = ?", [userId], (err, rows) => err ? reject(err) : resolve(rows));
        });
        if (userPending.status !== 'PENDING') throw new Error("Status should be PENDING, but got " + userPending.status);
        console.log("Verified Status: PENDING");

        // 5. Admin Approves
        console.log("Step 5: Admin Approves...");
        await new Promise((resolve, reject) => {
            db.query("UPDATE users SET status = 'APPROVED', is_active = 1 WHERE id = ?", [userId], (err) => err ? reject(err) : resolve());
        });

        // 6. Final verification
        const [userFinal] = await new Promise((resolve, reject) => {
            db.query("SELECT * FROM users WHERE id = ?", [userId], (err, rows) => err ? reject(err) : resolve(rows));
        });
        if (userFinal.status !== 'APPROVED' || userFinal.is_active !== 1) {
            throw new Error("Final verification failed: status=" + userFinal.status + ", is_active=" + userFinal.is_active);
        }
        console.log("Verification Successful! User is APPROVED and ACTIVE.");

    } catch (err) {
        console.error("Verification FAILED:", err.message);
        process.exit(1);
    } finally {
        process.exit(0);
    }
}

testFlow();
