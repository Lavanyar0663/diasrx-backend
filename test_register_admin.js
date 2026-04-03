async function runTest() {
    try {
        console.log("Logging in as admin...");
        const loginRes = await fetch("http://localhost:5000/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email: "admin@diasrx.com", password: "admin", role: "admin" })
        });
        const loginData = await loginRes.json();
        const token = loginData.token;
        if (!token) {
            console.error("Login failed!", loginData);
            return;
        }
        console.log("Got token.");

        console.log("Registering doctor...");
        const payload = {
            name: "Dr. Admin Created",
            password: "password123",
            role: "doctor",
            phone: "9988776655",
            department: "General Dentistry"
        };
        const regRes = await fetch("http://localhost:5000/api/auth/register", {
            method: "POST",
            headers: { 
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(payload)
        });
        const regData = await regRes.json();
        console.log("Status:", regRes.status);
        console.log("Response:", regData);

    } catch (e) {
        console.error("Error:", e);
    }
}
runTest();
