const nodemailer = require('nodemailer');
require('dotenv').config();

const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS
    }
});

const testEmail = async () => {
    const email = process.env.EMAIL_USER; // Test sending to yourself
    console.log(`[TEST] Attempting to send test email to ${email}...`);
    
    const mailOptions = {
        from: `"DIAS Rx Direct Test" <${process.env.EMAIL_USER}>`,
        to: email,
        subject: 'DIAS Rx Connectivity Test',
        text: 'This is a test email from the DIAS Rx backend to verify your SMTP configuration.'
    };

    try {
        const info = await transporter.sendMail(mailOptions);
        console.log(`[SUCCESS] Email sent! Message ID: ${info.messageId}`);
        console.log(`[NOTE] Please check your inbox for ${email}`);
    } catch (error) {
        console.error(`[FAILURE] Error: ${error.message}`);
        if (error.message.includes('Invalid login')) {
            console.error('[HINT] Check if your Google App Password is correct and has no extra spaces.');
        } else if (error.message.includes('ETIMEDOUT')) {
            console.error('[HINT] Your network might be blocking Gmail. Try connecting to a different network (hotspot).');
        }
    }
};

testEmail();
