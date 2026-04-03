const db = require('./db');

async function checkDb() {
    try {
        const users = await new Promise((res, rej) => db.query("SELECT * FROM users WHERE name LIKE '%Beulah%'", (e, r) => e ? rej(e) : res(r)));
        const doctors = await new Promise((res, rej) => db.query("SELECT * FROM doctors WHERE name LIKE '%Beulah%'", (e, r) => e ? rej(e) : res(r)));
        
        console.log("USERS TABLE:");
        console.log(users);
        
        console.log("DOCTORS TABLE:");
        console.log(doctors);
        
    } catch (e) {
        console.error(e);
    }
    process.exit(0);
}
checkDb();
