package com.example.csci_310project2team26.data.repository;

import com.example.csci_310project2team26.data.model.Comment;
import com.example.csci_310project2team26.data.network.ApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

/**
 * CommentRepository - Handles comment data operations using backend API
 * Part of the Repository layer in MVVM architecture
 */
public class CommentRepository {

    public static class CommentsResult {
        private final List<Comment> comments;
        private final int count;

        public CommentsResult(List<Comment> comments, int count) {
            this.comments = comments;
            this.count = count;
        }

        public List<Comment> getComments() {
            return comments;
        }

        public int getCount() {
            return count;
        }
    }

    public static class VoteResult {
        private final String message;
        private final String action;
        private final String type;
        private final Comment comment;

        public VoteResult(String message, String action, String type, Comment comment) {
            this.message = message;
            this.action = action;
            this.type = type;
            this.comment = comment;
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

        public Comment getComment() {
            return comment;
        }
    }

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

    /**
     * Fetch comments for a post
     */
    public void fetchComments(String postId, Callback<CommentsResult> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                String authHeader = token != null ? "Bearer " + token : null;

                retrofit2.Call<ApiService.CommentsResponse> call = apiService.getComments(authHeader, postId);
                Response<ApiService.CommentsResponse> response = call.execute();
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.CommentsResponse commentsResponse = response.body();
                    List<Comment> comments = commentsResponse.comments != null ? commentsResponse.comments : new ArrayList<>();
                    int count = commentsResponse.count;
                    
                    callback.onSuccess(new CommentsResult(comments, count));
                } else {
                    callback.onError("Failed to load comments");
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    /**
     * Create a new comment
     */
    public void createComment(String postId, String text, String title, Callback<Comment> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                if (token == null) {
                    callback.onError("Authentication required");
                    return;
                }
                
                retrofit2.Call<ApiService.CommentResponse> call = apiService.createComment(
                    "Bearer " + token,
                    postId,
                    text,
                    title
                );
                
                Response<ApiService.CommentResponse> response = call.execute();
                
                if (response.isSuccessful() && response.body() != null && response.body().comment != null) {
                    callback.onSuccess(response.body().comment);
                } else {
                    String errorMsg = "Failed to create comment";
                    if (response.code() == 401) {
                        errorMsg = "Authentication required";
                    } else if (response.code() == 400) {
                        errorMsg = "Invalid comment data";
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
     * Vote on a comment
     */
    public void voteOnComment(String postId,
                              String commentId,
                              String type,
                              Callback<VoteResult> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                if (token == null) {
                    callback.onError("Authentication required");
                    return;
                }
                
                // First vote on the comment
                retrofit2.Call<ApiService.VoteActionResponse> call = apiService.voteComment(
                    "Bearer " + token,
                    commentId,
                    type
                );
                
                Response<ApiService.VoteActionResponse> response = call.execute();
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.VoteActionResponse voteResponse = response.body();
                    
                    // Fetch updated comment to get new vote counts
                    fetchComments(postId, new Callback<CommentsResult>() {
                        @Override
                        public void onSuccess(CommentsResult result) {
                            if (result == null || result.getComments() == null) {
                                callback.onError("Failed to fetch comments after voting");
                                return;
                            }
                            // Find the updated comment
                            Comment updatedComment = null;
                            for (Comment comment : result.getComments()) {
                                if (comment != null && comment.getId() != null && comment.getId().equals(commentId)) {
                                    updatedComment = comment;
                                    break;
                                }
                            }
                            
                            if (updatedComment != null) {
                                // When action is "removed", type might be null
                                String resultType = voteResponse.type != null ? voteResponse.type : 
                                                  ("removed".equals(voteResponse.action) ? null : type);
                                callback.onSuccess(new VoteResult(
                                    voteResponse.message != null ? voteResponse.message : "Vote recorded",
                                    voteResponse.action != null ? voteResponse.action : "created",
                                    resultType,
                                    updatedComment
                                ));
                            } else {
                                callback.onError("Comment not found after voting");
                            }
                        }
                        
                        @Override
                        public void onError(String error) {
                            callback.onError("Failed to refresh comment after voting");
                        }
                    });
                } else {
                    callback.onError("Failed to vote on comment");
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    /**
     * Fetch comments by a specific user
     */
    public void fetchCommentsByUser(String userId, Callback<List<Comment>> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                String authHeader = token != null ? "Bearer " + token : null;

                retrofit2.Call<ApiService.CommentsResponse> call = apiService.getCommentsByUser(authHeader, userId);
                Response<ApiService.CommentsResponse> response = call.execute();
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.CommentsResponse commentsResponse = response.body();
                    List<Comment> comments = commentsResponse.comments != null ? commentsResponse.comments : new ArrayList<>();
                    callback.onSuccess(comments);
                } else {
                    callback.onError("Failed to load comments");
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    /**
     * Get a single comment by ID
     */
    public void getCommentById(String postId,
                               String commentId,
                               Callback<Comment> callback) {
        executorService.execute(() -> {
            try {
                // Fetch all comments for the post and find the one we need
                fetchComments(postId, new Callback<CommentsResult>() {
                    @Override
                    public void onSuccess(CommentsResult result) {
                        for (Comment comment : result.getComments()) {
                            if (comment.getId().equals(commentId)) {
                                callback.onSuccess(comment);
                                return;
                            }
                        }
                        callback.onError("Comment not found");
                    }
                    
                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                });
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    /**
     * Update a comment
     */
    public void updateComment(String postId,
                              String commentId,
                              String newText,
                              String newTitle,
                              Callback<Comment> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                if (token == null) {
                    callback.onError("Authentication required");
                    return;
                }
                
                retrofit2.Call<ApiService.CommentResponse> call = apiService.updateComment(
                    "Bearer " + token,
                    commentId,
                    newText,
                    newTitle
                );
                
                Response<ApiService.CommentResponse> response = call.execute();
                
                if (response.isSuccessful() && response.body() != null && response.body().comment != null) {
                    callback.onSuccess(response.body().comment);
                } else {
                    String errorMsg = "Failed to update comment";
                    if (response.code() == 401) {
                        errorMsg = "Authentication required";
                    } else if (response.code() == 403) {
                        errorMsg = "You can only edit your own comments";
                    } else if (response.code() == 404) {
                        errorMsg = "Comment not found";
                    }
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    /**
     * Delete a comment
     */
    public void deleteComment(String commentId, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                if (token == null) {
                    callback.onError("Authentication required");
                    return;
                }
                
                retrofit2.Call<Void> call = apiService.deleteComment(
                    "Bearer " + token,
                    commentId
                );
                
                Response<Void> response = call.execute();
                
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    String errorMsg = "Failed to delete comment";
                    if (response.code() == 401) {
                        errorMsg = "Authentication required";
                    } else if (response.code() == 403) {
                        errorMsg = "You can only delete your own comments";
                    } else if (response.code() == 404) {
                        errorMsg = "Comment not found";
                    }
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }
}
