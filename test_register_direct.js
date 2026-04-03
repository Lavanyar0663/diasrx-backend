const authController = require('./controllers/authController');

async function testDirect() {
    console.log("Mocking Express Response...");
    const req = {
        body: {
            name: "Dr. Admin Created",
            password: "password123",
            role: "doctor",
            phone: "9988776655",
            department: "General Dentistry"
        }
    };
    
    // We only need an object that mimics Express res methods (status, json, errors throwing)
    const res = {
        status: function(code) {
            this.statusCode = code;
            return this;
        },
        json: function(data) {
            console.log("SUCCESS. HTTP Code:", this.statusCode);
            console.log("Payload:", data);
        }
    };

    try {
        // authController.register is wrapped in express-async-handler
        // but we can call it. It takes (req, res, next)
        await authController.register(req, res, (err) => {
            console.error("Called next() with error:", err);
        });
    } catch (e) {
        console.error("Direct Throw:", e);
    }
}

testDirect().then(() => {
    // wait for async logs
    setTimeout(() => process.exit(0), 1000);
});
