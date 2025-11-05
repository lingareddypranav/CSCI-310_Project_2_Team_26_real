package com.example.csci_310project2team26.data.repository;
import com.example.csci_310project2team26.data.model.User;
import com.example.csci_310project2team26.data.network.ApiService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

/**
 * AuthRepository - Handles authentication data operations
 * Part of the Repository layer in MVVM architecture
 * 
 * Responsibilities:
 * - Register new USC users
 * - Authenticate users
 * - Manage user sessions
 * - Communicate with backend API
 */
public class AuthRepository {
    
    private final ApiService apiService;
    private final ExecutorService executorService;
    
    public AuthRepository() {
        this.apiService = ApiService.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Callback interface for async operations
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
    
    /**
     * Register a new USC user
     * 
     * @param name Full name
     * @param email USC email (@usc.edu)
     * @param studentId 10-digit USC student ID
     * @param password User password
     * @param callback Callback for result
     */
    public void register(String name, String email, String studentId, 
                        String password, Callback<String> callback) {
        executorService.execute(() -> {
            try {
                Call<RegisterResponse> call = apiService.register(name, email, studentId, password);
                Response<RegisterResponse> response = call.execute();
                
                if (response.isSuccessful() && response.body() != null) {
                    String userId = response.body().getUserId();
                    callback.onSuccess(userId);
                } else {
                    String errorMessage;
                    switch (response.code()) {
                        case 400:
                            errorMessage = "Invalid registration data";
                            break;
                        case 409:
                            errorMessage = "Email or Student ID already exists";
                            break;
                        default:
                            errorMessage = "Registration failed: " + response.message();
                            break;
                    }
                    callback.onError(errorMessage);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }
    
    /**
     * Login with USC email and password
     * 
     * @param email USC email
     * @param password User password
     * @param rememberMe Whether to save session
     * @param callback Callback for result
     */
    public void login(String email, String password, boolean rememberMe, 
                     Callback<User> callback) {
        executorService.execute(() -> {
            try {
                Call<LoginResponse> call = apiService.login(email, password);
                Response<LoginResponse> response = call.execute();
                
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    User user = loginResponse.getUser();
                    
                    // Save session if remember me is checked
                    if (rememberMe) {
                        saveSession(user, loginResponse.getToken());
                    }
                    
                    callback.onSuccess(user);
                } else {
                    String errorMessage;
                    switch (response.code()) {
                        case 401:
                            errorMessage = "Invalid email or password";
                            break;
                        case 404:
                            errorMessage = "User not found";
                            break;
                        default:
                            errorMessage = "Login failed: " + response.message();
                            break;
                    }
                    callback.onError(errorMessage);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }
    
    /**
     * Check if user has a saved session
     */
    public void checkSavedSession(Callback<User> callback) {
        executorService.execute(() -> {
            try {
                String token = getSavedToken();
                if (token != null) {
                    // Validate token with backend
                    Call<ValidateTokenResponse> call = apiService.validateToken("Bearer " + token);
                    Response<ValidateTokenResponse> response = call.execute();
                    
                    if (response.isSuccessful() && response.body() != null) {
                        callback.onSuccess(response.body().getUser());
                    } else {
                        clearSession();
                        callback.onSuccess(null);
                    }
                } else {
                    callback.onSuccess(null);
                }
            } catch (Exception e) {
                callback.onSuccess(null);
            }
        });
    }
    
    /**
     * Logout user and clear session
     */
    public void logout() {
        clearSession();
    }
    
    // Session management helpers
    private void saveSession(User user, String token) {
        // Minimal in-memory session storage (can be extended to SharedPreferences)
        SessionManager.setSession(token, user.getId());
    }
    
    private String getSavedToken() {
        return SessionManager.getToken();
    }
    
    private void clearSession() {
        SessionManager.clear();
    }
    
    /**
     * Data classes for API responses
     */
    public static class RegisterResponse {
        private String userId;
        private String message;
        
        public String getUserId() {
            return userId;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    public static class LoginResponse {
        private User user;
        private String token;
        
        public User getUser() {
            return user;
        }
        
        public String getToken() {
            return token;
        }
    }
    
    public static class ValidateTokenResponse {
        private User user;
        private boolean valid;
        
        public User getUser() {
            return user;
        }
        
        public boolean isValid() {
            return valid;
        }
    }
}
