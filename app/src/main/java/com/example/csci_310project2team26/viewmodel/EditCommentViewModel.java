package com.example.csci_310project2team26.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.csci_310project2team26.data.model.Comment;
import com.example.csci_310project2team26.data.repository.CommentRepository;

public class EditCommentViewModel extends ViewModel {

    private final CommentRepository commentRepository = new CommentRepository();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<Comment> comment = new MutableLiveData<>(null);
    private final MutableLiveData<Comment> updatedComment = new MutableLiveData<>(null);

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Comment> getComment() {
        return comment;
    }

    public LiveData<Comment> getUpdatedComment() {
        return updatedComment;
    }

    public void loadComment(String postId, String commentId) {
        if (postId == null || commentId == null) {
            return;
        }
        loading.postValue(true);
        error.postValue(null);
        commentRepository.getCommentById(postId, commentId, new CommentRepository.Callback<Comment>() {
            @Override
            public void onSuccess(Comment result) {
                loading.postValue(false);
                comment.postValue(result);
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
            }
        });
    }

    public void updateComment(String postId, String commentId, String text) {
        updateComment(postId, commentId, text, null);
    }

    public void updateComment(String postId, String commentId, String text, String title) {
        if (postId == null || commentId == null) {
            return;
        }
        loading.postValue(true);
        error.postValue(null);
        updatedComment.postValue(null);
        commentRepository.updateComment(postId, commentId, text, title, new CommentRepository.Callback<Comment>() {
            @Override
            public void onSuccess(Comment result) {
                loading.postValue(false);
                updatedComment.postValue(result);
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
            }
        });
    }
}
