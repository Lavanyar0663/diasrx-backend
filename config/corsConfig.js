let allowedOrigins = process.env.ALLOWED_ORIGINS
    ? process.env.ALLOWED_ORIGINS.split(",").map((o) => o.trim())
    : ["http://localhost:3000"];
allowedOrigins.push("http://localhost:5173");

const corsOptions = {
    origin: (origin, callback) => {
        // Allow requests with no origin (e.g., Postman, curl, same-origin server calls)
        if (!origin) {
            return callback(null, true);
        }

        if (allowedOrigins.includes(origin)) {
            callback(null, true);
        } else {
            callback(new Error(`CORS policy: Origin '${origin}' is not allowed.`));
        }
    },
    methods: ["GET", "POST", "PATCH"],
    allowedHeaders: ["Content-Type", "Authorization", "Idempotency-Key"],
    credentials: true,
};

module.exports = corsOptions;
