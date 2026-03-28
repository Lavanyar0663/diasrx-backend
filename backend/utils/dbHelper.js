const pool = require("../db");

/**
 * Utility function to handle database transactions safely.
 * Takes a callback function that receives the database connection.
 * Automatically commits on success and rolls back on error.
 */
const withTransaction = async (callback) => {
    const connection = await pool.promise().getConnection();
    await connection.beginTransaction();

    try {
        const result = await callback(connection);
        await connection.commit();
        return result;
    } catch (error) {
        await connection.rollback();
        throw error;
    } finally {
        connection.release();
    }
};

module.exports = {
    withTransaction
};
