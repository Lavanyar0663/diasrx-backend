const db = require("./db");

db.query("SHOW COLUMNS FROM patients", (err, res) => {
    if (err) {
        console.error(err);
    } else {
        const dateColumns = res.filter(c => c.Field.toLowerCase().includes('date') || c.Field.toLowerCase().includes('time') || c.Field.toLowerCase().includes('created'));
        console.log("DATE COLUMNS:", JSON.stringify(dateColumns, null, 2));
    }
    process.exit(0);
});
