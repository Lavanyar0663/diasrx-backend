package com.simats.frontend.network;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "DiasRxSession";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_PHONE = "user_phone";
    private static final String KEY_TITLE = "user_title";
    private static final String KEY_SPEC = "user_spec";
    private static final String KEY_AVATAR = "user_avatar";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(String token, String userId, String email, String role, String name) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_NAME, name);
        editor.apply();
    }

    public void updateProfile(String name, String phone, String title, String spec, String avatar) {
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_TITLE, title);
        editor.putString(KEY_SPEC, spec);
        editor.putString(KEY_AVATAR, avatar);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public String getUserEmail() {
        return getEmail();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public String getName() {
        return prefs.getString(KEY_NAME, null);
    }

    public String getUserName() {
        return getName();
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public String getPhone() { return prefs.getString(KEY_PHONE, ""); }
    public String getTitle() { return prefs.getString(KEY_TITLE, ""); }
    public String getSpec() { return prefs.getString(KEY_SPEC, ""); }
    public String getAvatar() { return prefs.getString(KEY_AVATAR, ""); }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    public void logout() {
        clearSession();
    }
}
