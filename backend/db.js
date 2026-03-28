require("dotenv").config();
const mysql = require("mysql2");

// Create a connection pool instead of a single connection
const pool = mysql.createPool({
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
  port: process.env.DB_PORT,   // ← comma added here
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
});

// Test the pool connection when starting up
pool.getConnection((err, connection) => {
  if (err) {
    console.error("Database connection failed:", err.message);
  } else {
    console.log("MySQL Pool Connected Successfully");
    connection.release();
  }
});

// Prevent unhandled 'error' events from crashing the process
pool.on("error", (err) => {
  console.error("MySQL pool error:", err.message);
});

// Export the pool
module.exports = pool;