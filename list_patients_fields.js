const db = require("./db");

db.query("SHOW COLUMNS FROM patients", (err, res) => {
    if (err) {
        console.error(err);
    } else {
        const fields = res.map(c => c.Field);
        console.log("ALL FIELDS:", fields);
    }
    process.exit(0);
});
