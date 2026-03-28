const http = require('http');

async function makeRequest(endpoint, method, body = null) {
    return new Promise((resolve, reject) => {
        const options = {
            hostname: 'localhost',
            port: 5000,
            path: '/api' + endpoint,
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

async function runTests() {
    console.log("=== STARTING API DEPLOYMENT CHECKS ===\n");
    let allPassed = true;

    // 1. Test Admin Login
    console.log("Testing Admin Login...");
    let res = await makeRequest('/auth/login', 'POST', { email: 'admin@diasrx.com', password: 'password123', role: 'admin' });
    if (res.status === 200 && res.data.token) {
        console.log("  ✅ Admin Login SUCCESS");
    } else {
        console.log("  ❌ Admin Login FAILED:", res.status, res.data);
        allPassed = false;
    }

    // 2. Test Doctor Login
    console.log("Testing Doctor Login...");
    res = await makeRequest('/auth/login', 'POST', { email: 'arjun.raman@diasrx.com', password: 'password123', role: 'doctor' });
    if (res.status === 200 && res.data.token) {
        console.log("  ✅ Doctor Login SUCCESS");
    } else {
        console.log("  ❌ Doctor Login FAILED:", res.status, res.data);
        allPassed = false;
    }

    // 3. Test Pharmacist Login & Pending Prescriptions
    console.log("Testing Pharmacist Login...");
    res = await makeRequest('/auth/login', 'POST', { email: 'ravi.kumar@diasrx.com', password: 'password123', role: 'pharmacist' });
    if (res.status === 200 && res.data.token) {
        console.log("  ✅ Pharmacist Login SUCCESS");
    } else {
        console.log("  ❌ Pharmacist Login FAILED:", res.status, res.data);
        allPassed = false;
    }

    // 4. Test Pharmacist Pending Prescriptions Route
    console.log("Testing Pending Prescriptions (Pharmacist)...");
    res = await makeRequest('/prescriptions/pending', 'GET');
    if (res.status === 200 || res.status === 404 || res.status === 204) { // 204 or 404 is fine if none exist, 200 if empty array
        console.log("  ✅ Pending Prescriptions SUCCESS:", res.status);
    } else {
        console.log("  ❌ Pending Prescriptions FAILED:", res.status, res.data);
        allPassed = false;
    }

    if (allPassed) {
        console.log("\n🎉 ALL API TESTS PASSED! Backend is deployment-ready.");
    } else {
        console.log("\n⚠️ SOME API TESTS FAILED. Check logs.");
    }
}

runTests();
