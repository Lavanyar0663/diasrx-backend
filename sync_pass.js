const db = require("./db");
const hash = "$2b$10$UHSMx/kzkP92vFOMRTzkYuJ3p1reBSUG4h6QeDPstSq2gzpSY7lOC";

console.log("Updating all passwords...");

db.query("UPDATE doctors SET password = ?", [hash], (err) => {
    if (err) console.error(err);
    console.log("Doctors updated");
    db.query("UPDATE pharmacists SET password = ?", [hash], (err) => {
        if (err) console.error(err);
        console.log("Pharmacists updated");
        db.query("UPDATE admin SET password = ?", [hash], (err) => {
            if (err) console.error(err);
            console.log("Admin updated");
            db.query("UPDATE users SET password = ?", [hash], (err) => {
                if (err) console.error(err);
                console.log("Users updated");
                process.exit(0);
            });
        });
    });
});
