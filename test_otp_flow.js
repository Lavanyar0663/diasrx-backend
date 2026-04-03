const authController = require('./controllers/authController');
const db = require('./db');
const bcrypt = require('bcrypt');

async function testFlow() {
    const email = 'meena.krishnan@diasrx.com';
    console.log(`--- Testing Forgot Password Flow for ${email} ---`);

    // 1. Forgot Password (Request OTP)
    const req1 = { body: { email } };
    let resData1 = null;
    const res1 = {
        json: (data) => { resData1 = data; },
        status: (code) => ({ json: (data) => { resData1 = { code, ...data }; } })
    };

    console.log("1. Requesting OTP...");
    await authController.forgotPassword(req1, res1, (err) => { if (err) console.error("Error 1:", err); });
    console.log("Response:", resData1);

    if (!resData1 || !resData1.otp) {
        console.error("Failed to get OTP");
        process.exit(1);
    }
    const otp = resData1.otp;

    // 2. Verify OTP
    const req2 = { body: { email, otp } };
    let resData2 = null;
    const res2 = {
        json: (data) => { resData2 = data; },
        status: (code) => ({ json: (data) => { resData2 = { code, ...data }; } })
    };

    console.log("\n2. Verifying OTP...");
    await authController.verifyOtp(req2, res2, (err) => { if (err) console.error("Error 2:", err); });
    console.log("Response:", resData2);

    if (!resData2 || resData2.message !== "OTP verified successfully.") {
        console.error("OTP verification failed");
        process.exit(1);
    }

    // 3. Reset Password
    const newPassword = "NewSecurePassword123!";
    const req3 = { body: { email, otp, newPassword } };
    let resData3 = null;
    const res3 = {
        json: (data) => { resData3 = data; },
        status: (code) => ({ json: (data) => { resData3 = { code, ...data }; } })
    };

    console.log("\n3. Resetting Password...");
    await authController.resetPassword(req3, res3, (err) => { if (err) console.error("Error 3:", err); });
    console.log("Response:", resData3);

    if (!resData3 || resData3.message !== "Password updated successfully.") {
        console.error("Password reset failed");
        process.exit(1);
    }

    // 4. Verify Password in DB
    console.log("\n4. Verifying password in database...");
    db.query("SELECT password FROM users WHERE email = ?", [email], async (err, rows) => {
        if (err) {
            console.error("DB Error:", err);
            process.exit(1);
        }
        const hashedPassword = rows[0].password;
        const match = await bcrypt.compare(newPassword, hashedPassword);
        if (match) {
            console.log("SUCCESS: Password updated and verified in database!");
        } else {
            console.error("FAILURE: Password in database does not match!");
        }
        process.exit(0);
    });
}

testFlow().catch(err => {
    console.error("Test execution error:", err);
    process.exit(1);
});
