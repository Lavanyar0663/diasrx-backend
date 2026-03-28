package com.simats.frontend.network;

public class Config {
    // Set to true for production/deployment, false for local development
    public static final boolean IS_PRODUCTION = false;

    // Local Development URL
    // Use your PC's IP address (172.25.82.29) for physical devices on same Wi-Fi
    // Use 10.0.2.2 for Android Emulator
    // Use 127.0.0.1 if using 'adb reverse tcp:5000 tcp:5000' with USB
    private static final String DEV_URL = "https://shani-unmauled-ginny.ngrok-free.dev/";

    // Production/Deployment URL (Example)
    private static final String PROD_URL = "https://your-diasrx-backend.herokuapp.com/";

    public static String getBaseUrl() {
        return IS_PRODUCTION ? PROD_URL : DEV_URL;
    }
}
