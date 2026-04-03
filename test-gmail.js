const nodemailer = require('nodemailer');
require('dotenv').config();

const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS
    }
});

console.log("Checking Gmail connection...");
console.log("User:", process.env.EMAIL_USER);
console.log("Password Length:", process.env.EMAIL_PASS ? process.env.EMAIL_PASS.length : 0);

transporter.verify((error, success) => {
    if (error) {
        console.error("CONNECTION FAILED:", error.message);
        if (error.message.includes("Invalid login")) {
            console.log("HINT: Your Google App Password might be incorrect or was revoked.");
        }
        process.exit(1);
    } else {
        console.log("SUCCESS: Server is ready to send emails!");
        
        const mailOptions = {
            from: process.env.EMAIL_USER,
            to: process.env.EMAIL_USER,
            subject: 'DIAS Rx Connectivity Test',
            text: 'If you see this, your Gmail connection is 100% fixed!'
        };
        
        transporter.sendMail(mailOptions, (err, info) => {
            if (err) {
                console.error("SEND FAILED:", err.message);
                process.exit(1);
            } else {
                console.log("TEST EMAIL SENT:", info.response);
                process.exit(0);
            }
        });
    }
});
