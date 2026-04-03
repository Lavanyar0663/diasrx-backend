const db = require("./db");
db.query("SELECT id, name, email, role, is_active FROM users WHERE role = 'doctor'", (err, rows) => {
    if (err) throw err;
    console.log(rows);
    process.exit(0);
});
