package com.example.csci_310project2team26.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.repository.PostRepository;

public class EditPostViewModel extends ViewModel {

    private final PostRepository postRepository = new PostRepository();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<Post> post = new MutableLiveData<>(null);
    private final MutableLiveData<Post> updatedPost = new MutableLiveData<>(null);

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Post> getPost() {
        return post;
    }

    public LiveData<Post> getUpdatedPost() {
        return updatedPost;
    }

    public void loadPost(String postId) {
        if (postId == null) {
            return;
        }
        loading.postValue(true);
        error.postValue(null);
        postRepository.getPostById(postId, new PostRepository.Callback<Post>() {
            @Override
            public void onSuccess(Post result) {
                loading.postValue(false);
                post.postValue(result);
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
            }
        });
    }

    public void updatePost(String postId, String title, String content, String tag, boolean isPrompt) {
        if (postId == null) {
            return;
        }
        loading.postValue(true);
        error.postValue(null);
        updatedPost.postValue(null);
        postRepository.updatePost(postId, title, content, tag, isPrompt, new PostRepository.Callback<Post>() {
            @Override
            public void onSuccess(Post result) {
                loading.postValue(false);
                updatedPost.postValue(result);
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
            }
        });
    }
}
