package com.example.csci_310project2team26.data.repository;

import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.model.PostVersion;
import com.example.csci_310project2team26.data.network.ApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class VersionRepository {

    private final ApiService apiService;
    private final ExecutorService executorService;

    public VersionRepository() {
        this.apiService = ApiService.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void getPostVersions(String postId, Callback<List<PostVersion>> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                if (token == null) {
                    callback.onError("Authentication required");
                    return;
                }

                retrofit2.Call<ApiService.VersionsResponse> call = apiService.getPostVersions(
                    "Bearer " + token,
                    postId
                );

                Response<ApiService.VersionsResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<PostVersion> versions = response.body().versions != null 
                        ? response.body().versions 
                        : new ArrayList<>();
                    callback.onSuccess(versions);
                } else {
                    String errorMsg = "Failed to load versions";
                    if (response.code() == 401) {
                        errorMsg = "Authentication required";
                    } else if (response.code() == 403) {
                        errorMsg = "You can only view versions of your own posts";
                    } else if (response.code() == 404) {
                        errorMsg = "Post not found";
                    }
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    public void revertToVersion(String postId, String versionId, Callback<Post> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                if (token == null) {
                    callback.onError("Authentication required");
                    return;
                }

                retrofit2.Call<ApiService.PostResponse> call = apiService.revertToVersion(
                    "Bearer " + token,
                    postId,
                    versionId
                );

                Response<ApiService.PostResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null && response.body().post != null) {
                    callback.onSuccess(response.body().post);
                } else {
                    String errorMsg = "Failed to revert to version";
                    if (response.code() == 401) {
                        errorMsg = "Authentication required";
                    } else if (response.code() == 403) {
                        errorMsg = "You can only revert your own posts";
                    } else if (response.code() == 404) {
                        errorMsg = "Post or version not found";
                    }
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }
}

