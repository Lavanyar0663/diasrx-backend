const { createLogger, format, transports } = require("winston");
const path = require("path");
const fs = require("fs");

// Ensure logs directory exists
const logsDir = path.join(__dirname, "../logs");
if (!fs.existsSync(logsDir)) {
    fs.mkdirSync(logsDir, { recursive: true });
}

const logger = createLogger({
    level: process.env.NODE_ENV === "production" ? "warn" : "info",
    format: format.combine(
        format.timestamp({ format: "YYYY-MM-DD HH:mm:ss" }),
        format.errors({ stack: true }),
        format.printf(({ timestamp, level, message, stack }) => {
            return stack
                ? `[${timestamp}] ${level.toUpperCase()}: ${message}\n${stack}`
                : `[${timestamp}] ${level.toUpperCase()}: ${message}`;
        })
    ),
    transports: [
        // Console output
        new transports.Console({
            format: format.combine(
                format.colorize(),
                format.printf(({ timestamp, level, message }) =>
                    `[${timestamp}] ${level}: ${message}`
                )
            ),
        }),
        // Persistent file log
        new transports.File({
            filename: path.join(logsDir, "app.log"),
            maxsize: 5242880,   // 5MB per file
            maxFiles: 5,
            tailable: true,
        }),
    ],
});

module.exports = logger;
