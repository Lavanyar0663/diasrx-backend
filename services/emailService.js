const nodemailer = require('nodemailer');
require('dotenv').config();

const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS
    }
});

const sendOtpEmail = async (email, otp) => {
    try {
        const mailOptions = {
            from: `"DIAS Rx Support" <${process.env.EMAIL_USER}>`,
            to: email,
            subject: 'Your DIAS Rx Password Reset OTP',
            html: `
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e1e1e1; border-radius: 10px;">
                    <h2 style="color: #009688; text-align: center;">DIAS Rx Password Reset</h2>
                    <p>Hello,</p>
                    <p>You requested a password reset for your DIAS Rx account. Use the following 6-digit OTP to verify your identity:</p>
                    <div style="background-color: #f5f5f5; padding: 20px; text-align: center; font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #333; border-radius: 5px; margin: 20px 0;">
                        ${otp}
                    </div>
                    <p>This OTP is valid for 5 minutes. If you did not request this reset, please ignore this email or contact support if you have concerns.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="font-size: 12px; color: #777; text-align: center;">DIAS Rx Clinical Management System</p>
                </div>
            `
        };

        const info = await transporter.sendMail(mailOptions);
        console.log(`[EMAIL_SERVICE] OTP email sent to ${email}: ${info.messageId}`);
        return true;
    } catch (error) {
        console.error(`[EMAIL_SERVICE] Error sending email to ${email}:`, error.message);
        return false;
    }
};

module.exports = {
    sendOtpEmail
};
