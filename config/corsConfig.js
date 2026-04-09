let allowedOrigins = process.env.ALLOWED_ORIGINS
    ? process.env.ALLOWED_ORIGINS.split(",").map((o) => o.trim().replace(/\/$/, ""))
    : [
        "http://localhost:3000",
        "http://localhost:3001",
        "http://localhost:3002",
        "http://localhost:3003",
        "http://localhost:3004",
        "http://localhost:3005",
        "http://localhost:5173",
        "http://180.235.121.253:8167",
        "http://14.139.187.229:8074"
    ];

const corsOptions = {
    origin: (origin, callback) => {
        // Allow requests with no origin (e.g., Postman, curl, same-origin server calls)
        if (!origin) {
            return callback(null, true);
        }

        // Normalize incoming origin by removing trailing slash for comparison
        const normalizedOrigin = origin.replace(/\/$/, "");

        if (allowedOrigins.includes(normalizedOrigin)) {
            callback(null, true);
        } else {
            console.error(`CORS Blocked: Origin '${origin}' (normalized: '${normalizedOrigin}') not in`, allowedOrigins);
            callback(new Error(`CORS policy: Origin '${origin}' is not allowed.`));
        }
    },
    methods: ["GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"],
    allowedHeaders: ["Content-Type", "Authorization", "Idempotency-Key"],
    credentials: true,
    optionsSuccessStatus: 200 // Some legacy browsers (IE11, various SmartTVs) choke on 204
};

module.exports = corsOptions;
