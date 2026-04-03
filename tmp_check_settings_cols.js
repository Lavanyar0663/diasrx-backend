const db = require("./db");
db.query("DESCRIBE users", (err, rows) => {
    if (err) {
        process.exit(1);
    }
    const cols = rows.map(r => r.Field);
    console.log("isTwoFactorEnabled:", cols.includes("isTwoFactorEnabled") ? "YES" : "NO");
    console.log("isNotificationsEnabled:", cols.includes("isNotificationsEnabled") ? "YES" : "NO");
    process.exit(0);
});
