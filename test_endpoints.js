const axios = require('axios');
require('dotenv').config();

async function test() {
    try {
        console.log('--- TESTING DASHBOARD ENDPOINTS ---');
        
        // 1. Login as Arjun
        const loginRes = await axios.post('http://localhost:3007/api/auth/login', {
            email: 'arjun@diasrx.com',
            password: 'password123' // Default password for testing
        });
        const token = loginRes.data.token;
        console.log('Login successful, token received.');

        const config = { headers: { Authorization: `Bearer ${token}` } };

        // 2. Test getDoctorDashboardInfo
        const infoRes = await axios.get('http://localhost:3007/api/patients/dashboard-info', config);
        console.log('\n--- DASHBOARD INFO ---');
        console.log(infoRes.data);

        // 3. Test getDoctorStats
        const statsRes = await axios.get('http://localhost:3007/api/patients/stats', config);
        console.log('\n--- DOCTOR STATS ---');
        console.log(statsRes.data);

    } catch (err) {
        console.error('Test failed:', err.response ? err.response.data : err.message);
    }
}

test();
