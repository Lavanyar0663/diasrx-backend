const db = require('./db');
db.query("DESCRIBE users", (err, res) => {
    if (err) console.error("Users Error:", err);
    else require('fs').writeFileSync('users_schema.json', JSON.stringify(res, null, 2));
    process.exit();
});
