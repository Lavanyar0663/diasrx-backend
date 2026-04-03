async function testRegister() {
    try {
        const payload = {
            name: "Dr. Test User",
            password: "password123",
            role: "doctor",
            phone: "9876543210",
            department: "Pediatric Dentistry"
        };
        console.log("Sending payload:", payload);
        const res = await fetch('http://localhost:5000/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        console.log("Status:", res.status);
        console.log("Response:", data);
    } catch (error) {
        console.error("Error:", error);
    }
}

testRegister();
