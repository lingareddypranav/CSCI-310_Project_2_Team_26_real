package com.example.csci_310project2team26.data.repository;

import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.network.ApiService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

/**
 * PostRepository - Handles posts feed, details, creation, and voting.
 */
public class PostRepository {

    private final ApiService apiService;
    private final ExecutorService executorService;

    public PostRepository() {
        this.apiService = ApiService.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void fetchPosts(String sort, Integer limit, Integer offset, Boolean isPromptPost,
                           Callback<ApiService.PostsResponse> callback) {
        executorService.execute(() -> {
            try {
                Call<ApiService.PostsResponse> call = apiService.getPosts(sort, limit, offset, isPromptPost);
                Response<ApiService.PostsResponse> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load posts: " + response.message());
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    public void getPostById(String postId, Callback<Post> callback) {
        executorService.execute(() -> {
            try {
                Call<ApiService.PostResponse> call = apiService.getPostById(postId);
                Response<ApiService.PostResponse> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().post);
                } else {
                    callback.onError("Failed to load post: " + response.message());
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    public void createPost(String title, String content, String llmTag, boolean isPromptPost,
                           Callback<Post> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                Call<ApiService.PostResponse> call = apiService.createPost(
                        token != null ? ("Bearer " + token) : null,
                        title, content, llmTag, isPromptPost
                );
                Response<ApiService.PostResponse> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().post);
                } else {
                    callback.onError("Failed to create post: " + response.message());
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    public void votePost(String postId, String type, Callback<ApiService.VoteActionResponse> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                Call<ApiService.VoteActionResponse> call = apiService.votePost(
                        token != null ? ("Bearer " + token) : null,
                        postId, type
                );
                Response<ApiService.VoteActionResponse> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to vote: " + response.message());
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }
}


