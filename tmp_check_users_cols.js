const db = require("./db");
db.query("DESCRIBE users", (err, rows) => {
    if (err) {
        console.error(err);
        process.exit(1);
    }
    console.log(JSON.stringify(rows, null, 2));
    process.exit(0);
});
