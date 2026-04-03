const db = require('./db');
db.query("UPDATE patients SET is_visited = 1 WHERE id % 2 = 0", (err, res) => {
    if (err) console.error(err);
    else console.log("Updated visited patients");
    process.exit();
});
