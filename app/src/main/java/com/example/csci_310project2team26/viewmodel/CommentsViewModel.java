package com.example.csci_310project2team26.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.csci_310project2team26.data.model.Comment;
import com.example.csci_310project2team26.data.repository.CommentRepository;
import com.example.csci_310project2team26.data.network.ApiService;

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

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<List<Comment>> getComments() { return comments; }

    public void loadComments(String postId) {
        loading.postValue(true);
        error.postValue(null);
        commentRepository.fetchComments(postId, new CommentRepository.Callback<ApiService.CommentsResponse>() {
            @Override
            public void onSuccess(ApiService.CommentsResponse result) {
                loading.postValue(false);
                comments.postValue(result.comments != null ? result.comments : new ArrayList<>());
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
            }
        });
    }

    public void addComment(String postId, String text) {
        loading.postValue(true);
        error.postValue(null);
        commentRepository.createComment(postId, text, new CommentRepository.Callback<Comment>() {
            @Override
            public void onSuccess(Comment result) {
                loading.postValue(false);
                List<Comment> current = comments.getValue();
                if (current == null) current = new ArrayList<>();
                current.add(0, result);
                comments.postValue(current);
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
            }
        });
    }
}


