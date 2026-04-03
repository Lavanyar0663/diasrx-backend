require('dotenv').config();

async function test() {
    try {
        console.log('--- TESTING DASHBOARD ENDPOINTS (Native Fetch @ Port 5000) ---');
        
        // 1. Login as Arjun
        const loginRes = await fetch('http://127.0.0.1:5000/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: 'arjun@diasrx.com',
                password: 'password123' 
            })
        });
        
        if (!loginRes.ok) throw new Error('Login failed: ' + await loginRes.text());
        
        const loginData = await loginRes.json();
        const token = loginData.token;
        console.log('Login successful, token received.');

        const config = { headers: { Authorization: `Bearer ${token}` } };

        // 2. Test getDoctorDashboardInfo
        const infoRes = await fetch('http://127.0.0.1:5000/api/patients/dashboard-info', config);
        const infoData = await infoRes.json();
        console.log('\n--- DASHBOARD INFO ---');
        console.log(infoData);

        // 3. Test getDoctorStats
        const statsRes = await fetch('http://127.0.0.1:5000/api/patients/stats', config);
        const statsData = await statsRes.json();
        console.log('\n--- DOCTOR STATS ---');
        console.log(statsData);

    } catch (err) {
        console.error('Test failed:', err.message);
    }
}

test();
