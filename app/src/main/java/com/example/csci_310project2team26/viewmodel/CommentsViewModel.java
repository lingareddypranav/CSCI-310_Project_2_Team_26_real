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
        loading.postValue(true);
        error.postValue(null);
        latestPostedComment.postValue(null);
        postingComment.postValue(true);
        commentRepository.createComment(postId, text, new CommentRepository.Callback<Comment>() {
            @Override
            public void onSuccess(Comment result) {
                loading.postValue(false);
                postingComment.postValue(false);
                List<Comment> current = comments.getValue();
                if (current == null) {
                    current = new ArrayList<>();
                } else {
                    current = new ArrayList<>(current);
                }
                current.add(0, result);
                comments.postValue(current);
                latestPostedComment.postValue(result);
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                postingComment.postValue(false);
                error.postValue(err);
            }
        });
    }

    public void voteOnComment(String postId, String commentId, String type) {
        commentRepository.voteOnComment(postId, commentId, type, new CommentRepository.Callback<CommentRepository.VoteResult>() {
            @Override
            public void onSuccess(CommentRepository.VoteResult result) {
                List<Comment> current = comments.getValue();
                if (current == null) {
                    current = new ArrayList<>();
                } else {
                    current = new ArrayList<>(current);
                }
                for (int i = 0; i < current.size(); i++) {
                    Comment item = current.get(i);
                    if (item.getId().equals(result.getComment().getId())) {
                        current.set(i, result.getComment());
                        break;
                    }
                }
                comments.postValue(current);
            }

            @Override
            public void onError(String err) {
                error.postValue(err);
            }
        });
    }
}