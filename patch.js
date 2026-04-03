const db = require('./db');
db.query("UPDATE patients p JOIN doctors d ON p.doctor_id = d.id SET p.doctor_id = NULL WHERE d.name LIKE '%Arjun%' AND p.department != 'Oral Surgery'", (err, res) => {
    if(err) console.error(err);
    else console.log(res.affectedRows + ' rows updated');
    process.exit();
});
