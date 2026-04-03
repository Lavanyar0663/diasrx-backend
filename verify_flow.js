const axios = require('axios');

const BASE_URL = 'http://localhost:5000/api';

async function testFlow() {
    try {
        console.log('--- 1. Admin Create Doctor ---');
        const doctorPayload = {
            name: "Dr. Test Execution",
            email: "test.dr.exec@diasrx.com",
            role: "doctor",
            mobile: "9998887776",
            department: "Dental",
            professionalTitle: "Chief Surgeon"
        };
        const regRes = await axios.post(`${BASE_URL}/auth/register`, doctorPayload);
        console.log('Register Success:', regRes.data);

        console.log('\n--- 2. Android Check User ---');
        const checkRes = await axios.get(`${BASE_URL}/admin/check-user`, {
            params: { email: doctorPayload.email }
        });
        console.log('Check User Result:', checkRes.data);

        console.log('\n--- 3. User Request Access ---');
        const requestPayload = {
            email: doctorPayload.email,
            role: "doctor",
            name: "Dr. Test Execution (Updated)",
            phone: "9998887776",
            department: "Orthodontics",
            professional_title: "Senior Surgeon"
        };
        const reqRes = await axios.post(`${BASE_URL}/auth/request-access`, requestPayload);
        console.log('Request Access Result:', reqRes.data);

        console.log('\n--- 4. Verify Pending Status ---');
        const finalCheck = await axios.get(`${BASE_URL}/admin/check-user`, {
            params: { email: doctorPayload.email }
        });
        console.log('Final Status:', finalCheck.data.status);

    } catch (error) {
        console.error('Test Failed:', error.response ? error.response.data : error.message);
    }
}

testFlow();
