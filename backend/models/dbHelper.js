const pool = require("../db");

const executeQuery = (query, params = []) => {
  return new Promise((resolve, reject) => {
    pool.query(query, params, (err, results) => {
      if (err) {
        return reject(err);
      }
      resolve(results);
    });
  });
};

const beginTransaction = () => {
  return new Promise((resolve, reject) => {
    pool.getConnection((err, connection) => {
      if (err) return reject(err);
      connection.beginTransaction((err) => {
        if (err) {
          connection.release();
          return reject(err);
        }
        resolve(connection);
      });
    });
  });
};

const commit = (connection) => {
  return new Promise((resolve, reject) => {
    connection.commit((err) => {
      if (err) {
        connection.rollback(() => {
          connection.release();
          reject(err);
        });
      } else {
        connection.release();
        resolve();
      }
    });
  });
};

const rollback = (connection) => {
  return new Promise((resolve, reject) => {
    connection.rollback(() => {
      connection.release();
      resolve();
    });
  });
};

module.exports = {
  executeQuery,
  beginTransaction,
  commit,
  rollback
};
