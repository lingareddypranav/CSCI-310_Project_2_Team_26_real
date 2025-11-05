package com.example.csci_310project2team26.data.network;

import com.example.csci_310project2team26.data.model.Profile;
import com.example.csci_310project2team26.data.repository.AuthRepository;
import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.model.Comment;

import java.util.List;

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
import retrofit2.http.Query;

/**
 * ApiService - RESTful API interface for BestLLM backend
 * Uses Retrofit for network communication
 * 
 * Base URL should be configured in BuildConfig or Constants
 */
public interface ApiService {
    
    String BASE_URL = "https://csci-310project2team26real-production.up.railway.app/"; // Hosted backend base URL
    
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
    
    // Profile endpoints (protected)
    @POST("api/profile/create")
    @FormUrlEncoded
    Call<Void> createProfile(
        @Header("Authorization") String token,
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
        @Header("Authorization") String token,
        @Path("userId") String userId,
        @Field("birth_date") String birthDate,
        @Field("bio") String bio,
        @Field("interests") String interests,
        @Field("profile_picture_url") String profilePictureUrl
    );
    
    @POST("api/profile/reset-password")
    @FormUrlEncoded
    Call<Void> resetPassword(
        @Header("Authorization") String token,
        @Field("user_id") String userId,
        @Field("current_password") String currentPassword,
        @Field("new_password") String newPassword
    );

    // Posts endpoints
    @GET("api/posts")
    Call<PostsResponse> getPosts(
        @Query("sort") String sort,
        @Query("limit") Integer limit,
        @Query("offset") Integer offset,
        @Query("is_prompt_post") Boolean isPromptPost
    );

    @GET("api/posts/{id}")
    Call<PostResponse> getPostById(@Path("id") String id);

    @POST("api/posts")
    @FormUrlEncoded
    Call<PostResponse> createPost(
        @Header("Authorization") String token,
        @Field("title") String title,
        @Field("content") String content,
        @Field("llm_tag") String llmTag,
        @Field("is_prompt_post") boolean isPromptPost
    );

    // Comments endpoints
    @GET("api/comments/{postId}")
    Call<CommentsResponse> getComments(@Path("postId") String postId);

    @POST("api/comments")
    @FormUrlEncoded
    Call<CommentResponse> createComment(
        @Header("Authorization") String token,
        @Field("post_id") String postId,
        @Field("text") String text
    );

    // Votes endpoints
    @POST("api/votes/post/{postId}")
    @FormUrlEncoded
    Call<VoteActionResponse> votePost(
        @Header("Authorization") String token,
        @Path("postId") String postId,
        @Field("type") String type
    );

    @GET("api/votes/post/{postId}")
    Call<VoteCountsResponse> getPostVoteCounts(@Path("postId") String postId);

    // Simple response wrappers
    class PostsResponse {
        public List<Post> posts;
        public int count;
    }

    class PostResponse {
        public Post post;
    }

    class CommentsResponse {
        public List<Comment> comments;
        public int count;
    }

    class CommentResponse {
        public Comment comment;
    }

    class VoteActionResponse {
        public String message;
        public String action;
        public String type;
    }

    class VoteCountsResponse {
        public int upvotes;
        public int downvotes;
        public int total;
    }
    
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
