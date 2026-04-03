const db = require("./db");

db.query("SHOW COLUMNS FROM patients", (err, res) => {
    if (err) {
        console.error(err);
    } else {
        const fields = res.map(c => c.Field);
        console.log("FIELDS_START");
        fields.forEach(f => console.log(f));
        console.log("FIELDS_END");
    }
    process.exit(0);
});
