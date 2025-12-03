package com.example.csci_310project2team26.data.repository;

import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.network.ApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class BookmarkRepository {

    private final ApiService apiService;
    private final ExecutorService executorService;

    public BookmarkRepository() {
        this.apiService = ApiService.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void addBookmark(String postId, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                if (token == null) {
                    callback.onError("Authentication required");
                    return;
                }

                retrofit2.Call<com.google.gson.JsonObject> call = apiService.addBookmark("Bearer " + token, postId);
                Response<com.google.gson.JsonObject> response = call.execute();

                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    String errorMsg = "Failed to add bookmark";
                    if (response.code() == 401) {
                        errorMsg = "Authentication required";
                    } else if (response.code() == 404) {
                        errorMsg = "Post not found";
                    } else if (response.code() == 409) {
                        errorMsg = "Post is already bookmarked";
                    }
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    public void removeBookmark(String postId, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                if (token == null) {
                    callback.onError("Authentication required");
                    return;
                }

                retrofit2.Call<Void> call = apiService.removeBookmark("Bearer " + token, postId);
                Response<Void> response = call.execute();

                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    String errorMsg = "Failed to remove bookmark";
                    if (response.code() == 401) {
                        errorMsg = "Authentication required";
                    } else if (response.code() == 404) {
                        errorMsg = "Bookmark not found";
                    }
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    public void getBookmarks(Boolean isPromptPost, Callback<List<Post>> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                if (token == null) {
                    callback.onError("Authentication required");
                    return;
                }

                retrofit2.Call<ApiService.PostsResponse> call = apiService.getBookmarks(
                    "Bearer " + token,
                    isPromptPost != null ? (isPromptPost ? "true" : "false") : null
                );

                Response<ApiService.PostsResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<Post> posts = response.body().posts != null ? response.body().posts : new ArrayList<>();
                    callback.onSuccess(posts);
                } else {
                    String errorMsg = "Failed to load bookmarks";
                    if (response.code() == 401) {
                        errorMsg = "Authentication required";
                    }
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    public void isBookmarked(String postId, Callback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                if (token == null) {
                    callback.onSuccess(false);
                    return;
                }

                retrofit2.Call<ApiService.BookmarkStatusResponse> call = apiService.isBookmarked("Bearer " + token, postId);
                Response<ApiService.BookmarkStatusResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().bookmarked);
                } else {
                    callback.onSuccess(false);
                }
            } catch (Exception e) {
                callback.onSuccess(false);
            }
        });
    }
}

