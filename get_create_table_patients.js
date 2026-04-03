const db = require("./db");

db.query("SHOW CREATE TABLE patients", (err, res) => {
    if (err) {
        console.error(err);
    } else {
        console.log("Full Create SQL:");
        console.log(res[0]['Create Table']);
    }
    process.exit(0);
});
