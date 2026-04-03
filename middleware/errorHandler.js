const logger = require("../config/logger");

// Centralized error handler — must be registered LAST in server.js
// eslint-disable-next-line no-unused-vars
const errorHandler = (err, req, res, next) => {
    // Determine status code
    const statusCode = err.status || err.statusCode || 500;

    // Log the error (always log stack in non-production)
    if (statusCode >= 500) {
        logger.error(`[${req.method}] ${req.originalUrl} → ${err.message}`, {
            stack: err.stack,
        });
    } else {
        logger.warn(`[${req.method}] ${req.originalUrl} → ${err.message}`);
    }

    // Build standardized response
    const response = {
        status: "error",
        message: err.message || "Something went wrong",
    };

    // Include stack trace in development only
    if (process.env.NODE_ENV !== "production") {
        response.details = err.stack;
    }

    res.status(statusCode).json(response);
};

module.exports = errorHandler;
