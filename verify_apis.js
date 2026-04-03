const http = require('http');

const API_BASE_URL = 'http://localhost:5000/api';

function makeRequest(options, postData) {
    return new Promise((resolve, reject) => {
        const req = http.request(options, (res) => {
            let data = '';
            res.on('data', (chunk) => data += chunk);
            res.on('end', () => {
                try {
                    resolve({ statusCode: res.statusCode, body: JSON.parse(data) });
                } catch (e) {
                    resolve({ statusCode: res.statusCode, body: data });
                }
            });
        });
        req.on('error', (e) => reject(e));
        if (postData) {
            req.write(JSON.stringify(postData));
        }
        req.end();
    });
}

async function verifyApis() {
    console.log('--- Verifying Backend APIs (Native HTTP) ---');

    // 1. Verify health check
    try {
        const res = await makeRequest({
            hostname: 'localhost',
            port: 5000,
            path: '/health',
            method: 'GET'
        });
        console.log('✅ Health Check:', res.statusCode, res.body);
    } catch (e) {
        console.error('❌ Health Check failed:', e.message);
    }

    // 2. Verify forgot password
    const db = require('./db');
    db.query("SELECT email FROM users LIMIT 1", async (err, rows) => {
        if (err || rows.length === 0) {
            console.error("No users to test forgot-password with.");
            db.end();
            return;
        }
        const testEmail = rows[0].email;
        console.log(`Testing Forgot Password with email: ${testEmail}`);

        try {
            const res = await makeRequest({
                hostname: 'localhost',
                port: 5000,
                path: '/api/auth/forgot-password',
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            }, { email: testEmail });
            console.log('✅ Forgot Password API:', res.statusCode, res.body);
        } catch (e) {
            console.error('❌ Forgot Password API failed:', e.message);
        }

        db.end();
        console.log('Verification script finished.');
    });
}

verifyApis();
