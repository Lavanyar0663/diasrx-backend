require('dotenv').config();

async function test() {
    try {
        console.log('--- DEBUGGING LOGIN RESPONSE ---');
        
        const loginRes = await fetch('http://127.0.0.1:5000/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: 'arjun@diasrx.com',
                password: 'password123' 
            })
        });
        
        const text = await loginRes.text();
        console.log('Status:', loginRes.status);
        console.log('Raw Response:', text.substring(0, 500));

    } catch (err) {
        console.error('Test failed:', err.message);
    }
}

test();
