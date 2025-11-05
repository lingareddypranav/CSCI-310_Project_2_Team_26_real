package com.example.csci_310project2team26.data.repository;

import com.example.csci_310project2team26.data.model.Comment;
import com.example.csci_310project2team26.data.network.ApiService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

/**
 * CommentRepository - Handles fetching and creating comments for posts.
 */
public class CommentRepository {

    private final ApiService apiService;
    private final ExecutorService executorService;

    public CommentRepository() {
        this.apiService = ApiService.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void fetchComments(String postId, Callback<ApiService.CommentsResponse> callback) {
        executorService.execute(() -> {
            try {
                Call<ApiService.CommentsResponse> call = apiService.getComments(postId);
                Response<ApiService.CommentsResponse> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load comments: " + response.message());
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    public void createComment(String postId, String text, Callback<Comment> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                Call<ApiService.CommentResponse> call = apiService.createComment(
                        token != null ? ("Bearer " + token) : null,
                        postId, text
                );
                Response<ApiService.CommentResponse> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().comment);
                } else {
                    callback.onError("Failed to create comment: " + response.message());
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }
}


