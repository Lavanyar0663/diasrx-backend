const db = require("./db");

db.query("SHOW COLUMNS FROM patients", (err, res) => {
    if (err) {
        console.error(err);
    } else {
        console.log("COLUMNS:", JSON.stringify(res, null, 2));
    }
    process.exit(0);
});
