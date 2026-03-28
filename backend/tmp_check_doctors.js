const db = require('./db');
db.query("DESCRIBE doctors", (err, res) => {
    if (err) console.error("Doctors Error:", err);
    else require('fs').writeFileSync('doctors_schema.json', JSON.stringify(res, null, 2));
    process.exit();
});
