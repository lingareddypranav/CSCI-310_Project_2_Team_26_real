package com.example.csci_310project2team26.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.csci_310project2team26.data.model.Comment;
import com.example.csci_310project2team26.data.repository.CommentRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * CommentsViewModel - Exposes comments for a post and comment creation.
 */
public class CommentsViewModel extends ViewModel {

    private final CommentRepository commentRepository = new CommentRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<List<Comment>> comments = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> postingComment = new MutableLiveData<>(false);
    private final MutableLiveData<Comment> latestPostedComment = new MutableLiveData<>(null);

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<List<Comment>> getComments() { return comments; }
    public LiveData<Boolean> isPostingComment() { return postingComment; }
    public LiveData<Comment> getLatestPostedComment() { return latestPostedComment; }

    public void loadComments(String postId) {
        loading.postValue(true);
        error.postValue(null);
        commentRepository.fetchComments(postId, new CommentRepository.Callback<CommentRepository.CommentsResult>() {
            @Override
            public void onSuccess(CommentRepository.CommentsResult result) {
                loading.postValue(false);
                comments.postValue(result.getComments() != null ? result.getComments() : new ArrayList<>());
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
                comments.postValue(new ArrayList<>());
            }
        });
    }

    public void addComment(String postId, String text) {
        addComment(postId, text, null);
    }

    public void addComment(String postId, String text, String title) {
        loading.postValue(true);
        error.postValue(null);
        latestPostedComment.postValue(null);
        postingComment.postValue(true);
        commentRepository.createComment(postId, text, title, new CommentRepository.Callback<Comment>() {
            @Override
            public void onSuccess(Comment result) {
                postingComment.postValue(false);
                latestPostedComment.postValue(result);
                // Reload comments from server to ensure we have the latest data
                // This ensures the comment count and all comments are up-to-date
                loadComments(postId);
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                postingComment.postValue(false);
                error.postValue(err);
            }
        });
    }

    public void editComment(String postId, String commentId, String text, String title) {
        loading.postValue(true);
        error.postValue(null);
        commentRepository.updateComment(postId, commentId, text, title, new CommentRepository.Callback<Comment>() {
            @Override
            public void onSuccess(Comment result) {
                loading.postValue(false);
                // Reload comments to get updated data
                loadComments(postId);
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
            }
        });
    }

    public void deleteComment(String postId, String commentId) {
        loading.postValue(true);
        error.postValue(null);
        commentRepository.deleteComment(commentId, new CommentRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                // Reload comments to reflect deletion
                loadComments(postId);
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
            }
        });
    }

    public void voteOnComment(String postId, String commentId, String type) {
        if (postId == null || commentId == null || type == null) {
            error.postValue("Invalid vote parameters");
            return;
        }
        
        loading.postValue(true);
        error.postValue(null);
        
        commentRepository.voteOnComment(postId, commentId, type, new CommentRepository.Callback<CommentRepository.VoteResult>() {
            @Override
            public void onSuccess(CommentRepository.VoteResult result) {
                loading.postValue(false);
                if (result == null || result.getComment() == null || result.getComment().getId() == null) {
                    error.postValue("Invalid vote result");
                    return;
                }
                
                List<Comment> current = comments.getValue();
                if (current == null) {
                    current = new ArrayList<>();
                } else {
                    current = new ArrayList<>(current);
                }
                
                String updatedCommentId = result.getComment().getId();
                boolean found = false;
                for (int i = 0; i < current.size(); i++) {
                    Comment item = current.get(i);
                    if (item != null && item.getId() != null && item.getId().equals(updatedCommentId)) {
                        current.set(i, result.getComment());
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    // Comment not found in list, reload all comments
                    loadComments(postId);
                } else {
                    comments.postValue(current);
                }
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err != null ? err : "Failed to vote on comment");
            }
        });
    }
}