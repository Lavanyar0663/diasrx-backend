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
5. Import `diasrx_database.sql` into MySQL/phpMyAdmin
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

## Database File

The repository includes a single database export:

- `diasrx_database.sql`

Import this file into the `diasrx` database using phpMyAdmin or MySQL CLI.

## Deployment Notes

- Do not upload `.env`; set its values in your hosting platform
- Run `npm install` during build/deploy
- Use `npm start` as the start command
- Provision a MySQL database and import `diasrx_database.sql`
- Set `ALLOWED_ORIGINS` to your frontend URL(s)
