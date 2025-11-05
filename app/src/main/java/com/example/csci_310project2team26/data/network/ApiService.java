package com.example.csci_310project2team26.data.network;

import com.example.csci_310project2team26.data.model.Profile;
import com.example.csci_310project2team26.data.repository.AuthRepository;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * ApiService - RESTful API interface for BestLLM backend
 * Uses Retrofit for network communication
 * 
 * Base URL should be configured in BuildConfig or Constants
 */
public interface ApiService {
    
    String BASE_URL = "csci-310project2team26-production.up.railway.app"; // Replace with actual backend URL
    
    // Authentication endpoints
    @POST("api/auth/register")
    @FormUrlEncoded
    Call<AuthRepository.RegisterResponse> register(
        @Field("name") String name,
        @Field("email") String email,
        @Field("student_id") String studentId,
        @Field("password") String password
    );
    
    @POST("api/auth/login")
    @FormUrlEncoded
    Call<AuthRepository.LoginResponse> login(
        @Field("email") String email,
        @Field("password") String password
    );
    
    @POST("api/auth/validate")
    Call<AuthRepository.ValidateTokenResponse> validateToken(
        @Header("Authorization") String token
    );
    
    @POST("api/auth/logout")
    Call<Void> logout(
        @Header("Authorization") String token
    );
    
    // Profile endpoints
    @POST("api/profile/create")
    @FormUrlEncoded
    Call<Void> createProfile(
        @Field("user_id") String userId,
        @Field("affiliation") String affiliation,
        @Field("birth_date") String birthDate,
        @Field("bio") String bio,
        @Field("interests") String interests,
        @Field("profile_picture_url") String profilePictureUrl
    );
    
    @GET("api/profile/{userId}")
    Call<Profile> getProfile(
        @Path("userId") String userId
    );
    
    @PUT("api/profile/{userId}")
    @FormUrlEncoded
    Call<Void> updateProfile(
        @Path("userId") String userId,
        @Field("birth_date") String birthDate,
        @Field("bio") String bio,
        @Field("interests") String interests,
        @Field("profile_picture_url") String profilePictureUrl
    );
    
    @POST("api/profile/reset-password")
    @FormUrlEncoded
    Call<Void> resetPassword(
        @Field("user_id") String userId,
        @Field("current_password") String currentPassword,
        @Field("new_password") String newPassword
    );
    
    /**
     * Singleton instance
     */
    class Factory {
        private static ApiService instance;
        
        public static ApiService getInstance() {
            if (instance == null) {
                Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
                
                instance = retrofit.create(ApiService.class);
            }
            return instance;
        }
    }
    
    static ApiService getInstance() {
        return Factory.getInstance();
    }
}
