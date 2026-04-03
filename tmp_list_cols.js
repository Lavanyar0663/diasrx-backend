const db = require("./db");
db.query("DESCRIBE users", (err, rows) => {
    if (err) {
        console.error(err);
        process.exit(1);
    }
    console.log("COLUMNS:", rows.map(r => r.Field).join(", "));
    process.exit(0);
});
