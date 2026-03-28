const http = require('http');

async function makeRequest(endpoint, method, body = null) {
    return new Promise((resolve, reject) => {
        const options = {
            hostname: 'localhost',
            port: 5000,
            path: '/api/auth' + endpoint,
            method: method,
            headers: {
                'Content-Type': 'application/json'
            }
        };

        const req = http.request(options, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => {
                try {
                    resolve({ status: res.statusCode, data: JSON.parse(data) });
                } catch (e) {
                    resolve({ status: res.statusCode, data });
                }
            });
        });

        req.on('error', e => reject(e));
        if (body) req.write(JSON.stringify(body));
        req.end();
    });
}

const users = [
    'admin@diasrx.com',
    'arjun.raman@diasrx.com',
    'ravi.kumar@diasrx.com',
    'meena.krishnan@diasrx.com'
];

async function run() {
    console.log("=== STARTING FORGOT PASSWORD ROLE TESTS ===\n");
    let allPassed = true;
    for (const email of users) {
        console.log(`Testing role for: ${email}`);
        try {
            // 1. Forgot Password
            let res = await makeRequest('/forgot-password', 'POST', { email });
            console.log(`  [1/3] Forgot Password -> Status: ${res.status}`);
            if (res.status !== 200) {
                console.error(`  ❌ Failed forgot password: ${JSON.stringify(res.data)}`);
                allPassed = false;
                continue;
            }
            const otp = res.data.otp;
            console.log(`  ✅ OTP received: ${otp}`);

            // 2. Verify OTP
            res = await makeRequest('/verify-otp', 'POST', { email, otp });
            console.log(`  [2/3] Verify OTP      -> Status: ${res.status}`);
            if (res.status !== 200) {
                console.error(`  ❌ Failed verify OTP: ${JSON.stringify(res.data)}`);
                allPassed = false;
                continue;
            }

            // 3. Reset Password
            res = await makeRequest('/reset-password', 'POST', { email, otp, newPassword: 'newpassword123' });
            console.log(`  [3/3] Reset Password  -> Status: ${res.status}`);
            if (res.status !== 200) {
                console.error(`  ❌ Failed reset password: ${JSON.stringify(res.data)}`);
                allPassed = false;
                continue;
            }
            console.log(`  ✅ Flow complete for ${email}\n`);
        } catch (e) {
            console.error(`  ❌ Error during test: ${e.message}`);
            allPassed = false;
        }
    }
    
    if (allPassed) {
        console.log("🎉 ALL TESTS PASSED! User roles fully supported in Forgot Password flow.");
    } else {
        console.log("⚠️ SOME TESTS FAILED.");
    }
}
run();
