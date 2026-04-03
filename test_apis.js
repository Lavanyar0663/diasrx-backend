const http = require("http");

const BASE_URL = "http://localhost:5000/api";

async function makeRequest(endpoint, method, body = null, token = null) {
    return new Promise((resolve, reject) => {
        const url = new URL(`${BASE_URL}${endpoint}`);
        const options = {
            hostname: url.hostname,
            port: url.port,
            path: url.pathname + url.search,
            method: method,
            headers: {
                "Content-Type": "application/json",
            },
        };

        if (token) {
            options.headers["Authorization"] = `Bearer ${token}`;
        }

        if (endpoint.includes("dispense")) {
            options.headers["Idempotency-Key"] = `test-idemp-${Date.now()}`;
        }

        const req = http.request(options, (res) => {
            let data = "";
            res.on("data", (chunk) => {
                data += chunk;
            });
            res.on("end", () => {
                try {
                    const parsed = JSON.parse(data);
                    resolve({ status: res.statusCode, data: parsed });
                } catch (e) {
                    resolve({ status: res.statusCode, data });
                }
            });
        });

        req.on("error", (e) => reject(e));

        if (body) {
            req.write(JSON.stringify(body));
        }
        req.end();
    });
}

async function runTests() {
    console.log("Starting API Tests...\n");

    try {
        // 1. Admin Login
        console.log("1. Testing Admin Login...");
        let res = await makeRequest("/auth/login", "POST", {
            email: "admin@diasrx.com",
            password: "password123",
            role: "admin",
        });
        console.log(`   Status: ${res.status}`);
        if (res.status !== 200) throw new Error("Admin login failed: " + JSON.stringify(res.data));
        const adminToken = res.data.token;
        console.log("   ✅ Admin Login Successful");

        // 2. Doctor Login
        console.log("\n2. Testing Doctor Login...");
        res = await makeRequest("/auth/login", "POST", {
            email: "arjun.raman@diasrx.com",
            password: "password123", // sync with the db
            role: "doctor",
        });
        console.log(`   Status: ${res.status}`);
        if (res.status !== 200) throw new Error("Doctor login failed: " + JSON.stringify(res.data));
        const doctorToken = res.data.token;
        console.log("   ✅ Doctor Login Successful");

        // 3. Pharmacist Login
        console.log("\n3. Testing Pharmacist Login...");
        res = await makeRequest("/auth/login", "POST", {
            email: "ravi.kumar@diasrx.com",
            password: "password123", // sync with the db
            role: "pharmacist",
        });
        console.log(`   Status: ${res.status}`);
        if (res.status !== 200) throw new Error("Pharmacist login failed: " + JSON.stringify(res.data));
        const pharmacistToken = res.data.token;
        console.log("   ✅ Pharmacist Login Successful");

        // 4. Get Patients (Admin)
        console.log("\n4. Testing GET /patients...");
        res = await makeRequest("/patients", "GET", null, adminToken);
        console.log(`   Status: ${res.status}`);
        if (res.status !== 200) throw new Error("Get patients failed");
        console.log(`   ✅ Fetched ${res.data.length} patients`);

        // 5. Get Drugs (Doctor)
        console.log("\n5. Testing GET /drugs...");
        res = await makeRequest("/drugs", "GET", null, doctorToken);
        console.log(`   Status: ${res.status}`);
        if (res.status !== 200) throw new Error("Get drugs failed");
        console.log(`   ✅ Fetched ${res.data.length} drugs in inventory`);

        // 6. Create Prescription (Doctor)
        console.log("\n6. Testing POST /prescriptions/full...");
        res = await makeRequest(
            "/prescriptions/full",
            "POST",
            {
                patient_id: 1,
                diagnosis: "Test Auto Diagnosis",
                remarks: "Testing APIs",
                drugs: [
                    {
                        drug_id: 1,
                        dosage: "500mg",
                        frequency: "1-0-1",
                        duration: "3 days",
                        quantity: 6,
                    },
                ],
            },
            doctorToken
        );
        console.log(`   Status: ${res.status}`);
        if (res.status !== 201) throw new Error("Create prescription failed: " + JSON.stringify(res.data));
        const prescriptionId = res.data.prescription.prescription_id;
        console.log(`   ✅ Created Prescription ID: ${prescriptionId}`);

        // 7. Get Pending Prescriptions (Pharmacist)
        console.log("\n7. Testing GET /prescriptions/pending...");
        res = await makeRequest("/prescriptions/pending", "GET", null, pharmacistToken);
        console.log(`   Status: ${res.status}`);
        if (res.status !== 200) throw new Error("Get pending prescriptions failed");
        console.log(`   ✅ Fetched ${res.data.length} pending prescriptions`);

        // 8. Dispense Prescription (Pharmacist)
        console.log(`\n8. Testing PATCH /prescriptions/${prescriptionId}/dispense...`);
        res = await makeRequest(`/prescriptions/${prescriptionId}/dispense`, "PATCH", null, pharmacistToken);
        console.log(`   Status: ${res.status}`);
        if (res.status !== 200) throw new Error("Dispense failed: " + JSON.stringify(res.data));
        console.log("   ✅ Dispensed Prescription successfully");

        console.log("\n🎉 ALL API TESTS PASSED SUCCESSFULLY!");
    } catch (error) {
        console.error("\n❌ TEST FAILED:");
        console.error(error.message);
    }
}

runTests();
