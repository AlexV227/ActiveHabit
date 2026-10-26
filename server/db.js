require("dotenv").config();
const { Pool } = require("pg");
const pool = process.env.DATABASE_URL
    ? new Pool({
        connectionString: process.env.DATABASE_URL
    })
    : new Pool();
module.exports = pool;