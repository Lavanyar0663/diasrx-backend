const db = require('./db');
db.query("UPDATE patients SET doctor_id = NULL WHERE doctor_id = 65 AND department != 'Oral Surgery'", (err, res) => {
    if (err) console.error(err);
    else console.log(res.affectedRows + ' rows updated');
    process.exit();
});
