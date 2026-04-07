# DIAS Rx Backend

Backend API for the DIAS Rx project.

## Requirements

- Node.js 18+
- npm 9+
- MySQL 8+

## Setup

1. Install dependencies:
   `npm install`
2. Copy `.env.example` to `.env`
3. Fill in your database, JWT, email, and CORS values
4. Create the MySQL database
5. Run the SQL files as needed for your environment
6. Start the server:
   `npm start`

The API runs on `PORT` from `.env`, or `5000` by default.

## Environment Variables

See `.env.example` for the required values:

- `PORT`
- `NODE_ENV`
- `DB_HOST`
- `DB_PORT`
- `DB_USER`
- `DB_PASSWORD`
- `DB_NAME`
- `JWT_SECRET`
- `EMAIL_USER`
- `EMAIL_PASS`
- `ALLOWED_ORIGINS`

## SQL Files

The repository includes these database files:

- `migration.sql`
- `database_migration_step1.sql`
- `dispense_logs_migration.sql`
- `otp_migration_email.sql`
- `seed_drugs.sql`

Apply the files required by your database state.

## Deployment Notes

- Do not upload `.env`; set its values in your hosting platform
- Run `npm install` during build/deploy
- Use `npm start` as the start command
- Provision a MySQL database and update the `DB_*` variables
- Set `ALLOWED_ORIGINS` to your frontend URL(s)
