const db = require('./db');
db.query("DESCRIBE patients", (err, res) => {
    if (err) console.error(err);
    else require('fs').writeFileSync('patients_schema.json', JSON.stringify(res, null, 2));
    process.exit();
});
