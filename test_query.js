const db = require('./db');
const fs = require('fs');
db.query('SELECT id, user_id, name FROM doctors', (err, rows) => {
  fs.writeFileSync('doctors.json', JSON.stringify(rows, null, 2), 'utf8');
  process.exit(0);
});
