console.log("SERVER SCRIPT STARTING...");
require("dotenv").config();
const express = require("express");
const cors = require("cors");
const helmet = require("helmet");
const morgan = require("morgan");
const path = require("path");

const logger = require("./config/logger");
const corsOptions = require("./config/corsConfig");
const errorHandler = require("./middleware/errorHandler");
const { generalLimiter } = require("./middleware/rateLimitMiddleware");
const db = require("./db");

const app = express();

// ── Security Headers ──────────────────────────────────────────────
app.use(helmet());

// ── Strict CORS ───────────────────────────────────────────────────
app.use(cors(corsOptions));

// ── Rate Limiting ─────────────────────────────────────────────────
// app.use(generalLimiter); // Temporarily disabled for debugging

app.use(express.json());

// ── Debug Request Logging ─────────────────────────────────────────
app.use((req, res, next) => {
  console.log(`[${new Date().toLocaleTimeString()}] ${req.method} ${req.url}`);
  next();
});

// ── HTTP Request Logging (morgan → winston) ───────────────────────
const morganStream = { write: (msg) => logger.info(msg.trim()) };
app.use(morgan("combined", { stream: morganStream }));

// ── Static Uploads ────────────────────────────────────────────────
app.use("/uploads", express.static(path.join(__dirname, "uploads")));

// ── Health Check ──────────────────────────────────────────────────
app.get("/health", (req, res) => {
  res.status(200).json({ status: "UP", timestamp: new Date().toISOString() });
});

// ── Application Routes ────────────────────────────────────────────
app.use("/api/auth", require("./routes/authRoutes"));
app.use("/api/doctors", require("./routes/doctorRoutes"));
app.use("/api/patients", require("./routes/patientRoutes"));
app.use("/api/drugs", require("./routes/drugRoutes"));
app.use("/api/prescriptions", require("./routes/prescriptionRoutes"));
app.use("/api/documents", require("./routes/documentRoutes"));
app.use("/api/admin", require("./routes/adminRoutes"));

// ── Centralized Error Handler (MUST be last) ──────────────────────
app.use(errorHandler);

// ── Server Start ──────────────────────────────────────────────────
const PORT = process.env.PORT || 5000;
const server = app.listen(PORT, () => {
  logger.info(`DIAS Rx server running on port ${PORT} [${process.env.NODE_ENV || "development"}]`);
});

// ── Graceful Shutdown ─────────────────────────────────────────────
const shutdown = (signal) => {
  logger.warn(`Received ${signal}. Starting graceful shutdown...`);

  server.close(() => {
    logger.info("HTTP server closed. No new connections accepted.");

    db.end((err) => {
      if (err) {
        logger.error("Error closing DB pool:", err.message);
        process.exit(1);
      }
      logger.info("MySQL connection pool closed. Process exiting cleanly.");
      process.exit(0);
    });
  });

  // Force kill if shutdown takes too long
  setTimeout(() => {
    logger.error("Graceful shutdown timed out. Forcing exit.");
    process.exit(1);
  }, 10000);
};

process.on("SIGTERM", () => shutdown("SIGTERM"));
process.on("SIGINT", () => shutdown("SIGINT"));

