package com.example.csci_310project2team26.data.repository;

import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.network.ApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

/**
 * PostRepository - Handles post data operations using backend API
 * Part of the Repository layer in MVVM architecture
 */
public class PostRepository {

    public static class PostsResult {
        private final List<Post> posts;
        private final int count;
        private final int limit;
        private final int offset;

        public PostsResult(List<Post> posts, int count, int limit, int offset) {
            this.posts = posts;
            this.count = count;
            this.limit = limit;
            this.offset = offset;
        }

        public List<Post> getPosts() {
            return posts;
        }

        public int getCount() {
            return count;
        }

        public int getLimit() {
            return limit;
        }

        public int getOffset() {
            return offset;
        }
    }

    public static class VoteActionResult {
        private final String message;
        private final String action;
        private final String type;

        public VoteActionResult(String message, String action, String type) {
            this.message = message;
            this.action = action;
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public String getAction() {
            return action;
        }

        public String getType() {
            return type;
        }
    }

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

    /**
     * Fetch posts with optional filtering and sorting
     */
    public void fetchPosts(String sort,
                           Integer limit,
                           Integer offset,
                           Boolean isPromptPost,
                           Callback<PostsResult> callback) {
        executorService.execute(() -> {
            try {
                retrofit2.Call<ApiService.PostsResponse> call = apiService.getPosts(
                    sort != null ? sort : "newest",
                    limit != null ? limit : 50,
                    offset != null ? offset : 0,
                    isPromptPost
                );
                
                Response<ApiService.PostsResponse> response = call.execute();
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.PostsResponse postsResponse = response.body();
                    List<Post> posts = postsResponse.posts != null ? postsResponse.posts : new ArrayList<>();
                    int count = postsResponse.count;
                    
                    callback.onSuccess(new PostsResult(
                        posts,
                        count,
                        limit != null ? limit : 50,
                        offset != null ? offset : 0
                    ));
                } else {
                    String errorMsg = "Failed to load posts";
                    if (response.code() == 401) {
                        errorMsg = "Authentication required";
                    } else if (response.code() == 500) {
                        errorMsg = "Server error";
                    }
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    /**
     * Search posts
     */
    public void searchPosts(String query,
                            String searchType,
                            String sort,
                            Integer limit,
                            Integer offset,
                            Boolean isPromptPost,
                            Callback<PostsResult> callback) {
        executorService.execute(() -> {
            try {
                // For prompt_tag search type, don't pass is_prompt_post as it's handled by the search_type
                Boolean promptFilter = null;
                if (searchType != null && !"prompt_tag".equals(searchType) && isPromptPost != null) {
                    promptFilter = isPromptPost;
                }
                
                retrofit2.Call<ApiService.PostsResponse> call = apiService.searchPosts(
                    query,
                    searchType != null ? searchType : "full_text",
                    limit != null ? limit : 50,
                    offset != null ? offset : 0,
                    promptFilter
                );
                
                Response<ApiService.PostsResponse> response = call.execute();
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.PostsResponse postsResponse = response.body();
                    List<Post> posts = postsResponse.posts != null ? postsResponse.posts : new ArrayList<>();
                    int count = postsResponse.count;
                    
                    callback.onSuccess(new PostsResult(
                        posts,
                        count,
                        limit != null ? limit : 50,
                        offset != null ? offset : 0
                    ));
                } else {
                    String errorMsg = "Failed to search posts";
                    if (response.code() == 400) {
                        errorMsg = "Invalid search query";
                    }
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    /**
     * Get single post by ID
     */
    public void getPostById(String postId, Callback<Post> callback) {
        executorService.execute(() -> {
            try {
                retrofit2.Call<ApiService.PostResponse> call = apiService.getPostById(postId);
                Response<ApiService.PostResponse> response = call.execute();
                
                if (response.isSuccessful() && response.body() != null && response.body().post != null) {
                    callback.onSuccess(response.body().post);
                } else {
                    callback.onError("Post not found");
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    /**
     * Create new post
     */
    public void createPost(String title,
                           String content,
                           String llmTag,
                           boolean isPromptPost,
                           String promptSection,
                           String descriptionSection,
                           boolean anonymous,
                           Callback<Post> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                if (token == null) {
                    callback.onError("Authentication required");
                    return;
                }

                // Normalize fields to avoid sending nulls in form data
                String safeTitle = title != null ? title.trim() : "";
                String safeContent = content != null ? content.trim() : "";
                String safeLlmTag = llmTag != null ? llmTag.trim() : "";

                // Only send prompt fields when the post is marked as a prompt post and the
                // sections contain real content. Otherwise, omit them to avoid the backend
                // inferring a prompt post from empty strings. This mirrors the Create page UI
                // where the prompt fields are hidden when the toggle is off.
                String safePromptSection = isPromptPost
                        ? (promptSection != null ? promptSection.trim() : "")
                        : null;
                String safeDescriptionSection = isPromptPost
                        ? (descriptionSection != null ? descriptionSection.trim() : "")
                        : null;

                boolean hasPromptContent = (safePromptSection != null && !safePromptSection.isEmpty())
                        || (safeDescriptionSection != null && !safeDescriptionSection.isEmpty());
                boolean normalizedIsPromptPost = isPromptPost && hasPromptContent;

                if (normalizedIsPromptPost) {
                    // Some backends still expect `content` to be populated even for prompt posts.
                    // Fall back to one of the prompt fields so the server never receives a null
                    // or empty content value when a prompt post is intended.
                    if (safeContent.isEmpty()) {
                        if (safeDescriptionSection != null && !safeDescriptionSection.isEmpty()) {
                            safeContent = safeDescriptionSection;
                        } else if (safePromptSection != null && !safePromptSection.isEmpty()) {
                            safeContent = safePromptSection;
                        }
                    }
                } else {
                    // Ensure prompt fields are completely omitted when treating the submission as
                    // a normal post to prevent server-side prompt validation from triggering.
                    safePromptSection = null;
                    safeDescriptionSection = null;
                }

                retrofit2.Call<ApiService.PostResponse> call = apiService.createPost(
                    "Bearer " + token,
                    safeTitle,
                    safeContent,
                    safeLlmTag,
                    normalizedIsPromptPost,
                    safePromptSection,
                    safeDescriptionSection,
                    anonymous
                );
                
                Response<ApiService.PostResponse> response = call.execute();
                
                if (response.isSuccessful() && response.body() != null && response.body().post != null) {
                    callback.onSuccess(response.body().post);
                } else {
                    String errorMsg = "Failed to create post";
                    if (response.code() == 401) {
                        errorMsg = "Authentication required";
                    } else {
                        // Try to surface the backend validation message when available
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                if (errorBody.contains("message")) {
                                    // The error payload is small; simple contains check avoids extra JSON parsing libs
                                    errorMsg = errorBody;
                                }
                            }
                        } catch (Exception ignored) {}

                        if (response.code() == 400) {
                            errorMsg = errorMsg.equals("Failed to create post") ? "Invalid post data" : errorMsg;
                        }
                    }
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    /**
     * Vote on a post
     */
    public void votePost(String postId, String type, Callback<VoteActionResult> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                if (token == null) {
                    callback.onError("Authentication required");
                    return;
                }
                
                retrofit2.Call<ApiService.VoteActionResponse> call = apiService.votePost(
                    "Bearer " + token,
                    postId,
                    type
                );
                
                Response<ApiService.VoteActionResponse> response = call.execute();
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.VoteActionResponse voteResponse = response.body();
                    if (voteResponse == null) {
                        callback.onError("Invalid vote response");
                        return;
                    }
                    
                    // Safely extract response fields with null checks
                    String responseAction = voteResponse.action != null ? voteResponse.action : "created";
                    String responseMessage = voteResponse.message != null ? voteResponse.message : "Vote recorded";
                    
                    // When action is "removed", type might be null in the response
                    String resultType;
                    if (voteResponse.type != null) {
                        resultType = voteResponse.type;
                    } else if ("removed".equals(responseAction)) {
                        resultType = null; // Vote was removed, no type
                    } else {
                        resultType = type; // Use the input type as fallback
                    }
                    
                    callback.onSuccess(new VoteActionResult(
                        responseMessage,
                        responseAction,
                        resultType
                    ));
                } else {
                    String errorMsg = "Failed to vote on post";
                    if (response.code() == 401) {
                        errorMsg = "Authentication required";
                    } else if (response.code() == 404) {
                        errorMsg = "Post not found";
                    } else if (response.code() == 400) {
                        errorMsg = "Invalid vote request";
                    }
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    /**
     * Fetch posts for a specific user (search by author)
     */
    public void fetchPostsForUser(String userId, Callback<List<Post>> callback) {
        executorService.execute(() -> {
            try {
                // Search for posts by author name (we'll need to get user name first or search)
                // For now, we'll fetch all posts and filter - not ideal but works
                retrofit2.Call<ApiService.PostsResponse> call = apiService.getPosts(
                    "newest",
                    100,
                    0,
                    null
                );
                
                Response<ApiService.PostsResponse> response = call.execute();
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Post> allPosts = response.body().posts != null ? response.body().posts : new ArrayList<>();
                    List<Post> userPosts = new ArrayList<>();
                    
                    for (Post post : allPosts) {
                        if (userId.equals(post.getAuthor_id())) {
                            userPosts.add(post);
                        }
                    }
                    
                    callback.onSuccess(userPosts);
                } else {
                    callback.onError("Failed to load user posts");
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    /**
     * Update a post
     */
    public void updatePost(String postId,
                           String title,
                           String content,
                           String llmTag,
                           boolean isPromptPost,
                           String promptSection,
                           String descriptionSection,
                           boolean anonymous,
                           Callback<Post> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                if (token == null) {
                    callback.onError("Authentication required");
                    return;
                }
                
                retrofit2.Call<ApiService.PostResponse> call = apiService.updatePost(
                    "Bearer " + token,
                    postId,
                    title,
                    content,
                    llmTag,
                    isPromptPost,
                    promptSection,
                    descriptionSection,
                    anonymous
                );
                
                Response<ApiService.PostResponse> response = call.execute();
                
                if (response.isSuccessful() && response.body() != null && response.body().post != null) {
                    callback.onSuccess(response.body().post);
                } else {
                    String errorMsg = "Failed to update post";
                    if (response.code() == 401) {
                        errorMsg = "Authentication required";
                    } else if (response.code() == 403) {
                        errorMsg = "You can only edit your own posts";
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

    /**
     * Delete a post
     */
    public void deletePost(String postId, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                if (token == null) {
                    callback.onError("Authentication required");
                    return;
                }
                
                retrofit2.Call<Void> call = apiService.deletePost(
                    "Bearer " + token,
                    postId
                );
                
                Response<Void> response = call.execute();
                
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    String errorMsg = "Failed to delete post";
                    if (response.code() == 401) {
                        errorMsg = "Authentication required";
                    } else if (response.code() == 403) {
                        errorMsg = "You can only delete your own posts";
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

    /**
     * Get prompt posts
     */
    public void fetchPromptPosts(String sort,
                                  Integer limit,
                                  Integer offset,
                                  Callback<PostsResult> callback) {
        executorService.execute(() -> {
            try {
                retrofit2.Call<ApiService.PostsResponse> call = apiService.getPromptPosts(
                    sort != null ? sort : "newest",
                    limit != null ? limit : 50,
                    offset != null ? offset : 0
                );
                
                Response<ApiService.PostsResponse> response = call.execute();
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.PostsResponse postsResponse = response.body();
                    List<Post> posts = postsResponse.posts != null ? postsResponse.posts : new ArrayList<>();
                    int count = postsResponse.count;
                    
                    callback.onSuccess(new PostsResult(
                        posts,
                        count,
                        limit != null ? limit : 50,
                        offset != null ? offset : 0
                    ));
                } else {
                    callback.onError("Failed to load prompt posts");
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    /**
     * Get trending posts
     */
    public void fetchTrendingPosts(Integer k, Callback<PostsResult> callback) {
        executorService.execute(() -> {
            try {
                retrofit2.Call<ApiService.PostsResponse> call = apiService.getTrendingPosts(
                    k != null ? k : 10
                );
                
                Response<ApiService.PostsResponse> response = call.execute();
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.PostsResponse postsResponse = response.body();
                    List<Post> posts = postsResponse.posts != null ? postsResponse.posts : new ArrayList<>();
                    int count = postsResponse.count;
                    
                    callback.onSuccess(new PostsResult(
                        posts,
                        count,
                        k != null ? k : 10,
                        0
                    ));
                } else {
                    callback.onError("Failed to load trending posts");
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }
}
