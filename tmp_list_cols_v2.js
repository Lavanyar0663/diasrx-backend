const db = require("./db");
db.query("DESCRIBE users", (err, rows) => {
    if (err) {
        console.error(err);
        process.exit(1);
    }
    const cols = rows.map(r => r.Field);
    console.log("FULL_COLUMNS_COUNT:", cols.length);
    console.log("COLUMNS_LIST:", JSON.stringify(cols));
    process.exit(0);
});
