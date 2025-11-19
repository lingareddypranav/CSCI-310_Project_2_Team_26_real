package com.example.csci_310project2team26.data.repository;

/**
 * SessionManager - Minimal in-memory session holder for auth token and user ID
 * Note: This is a simplified implementation to avoid Context wiring.
 * If persistence across process restarts is needed, wire this to SharedPreferences.
 */
public final class SessionManager {
    private static volatile String authToken;
    private static volatile String userId;

    private SessionManager() {}

    public static void setSession(String token, String uid) {
        authToken = token;
        userId = uid;
    }

    public static String getToken() {
        return authToken;
    }

    public static String getUserId() {
        return userId;
    }

    public static void clear() {
        authToken = null;
        userId = null;
    }
}


