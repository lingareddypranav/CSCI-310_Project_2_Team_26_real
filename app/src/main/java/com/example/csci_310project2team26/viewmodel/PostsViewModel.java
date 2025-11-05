package com.example.csci_310project2team26.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.repository.PostRepository;
import com.example.csci_310project2team26.data.network.ApiService;

import java.util.ArrayList;
import java.util.List;

/**
 * PostsViewModel - Exposes posts feed and actions.
 */
public class PostsViewModel extends ViewModel {

    private final PostRepository postRepository = new PostRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<List<Post>> posts = new MutableLiveData<>(new ArrayList<>());

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<List<Post>> getPosts() { return posts; }

    public void loadPosts(String sort, Integer limit, Integer offset, Boolean isPromptPost) {
        loading.postValue(true);
        error.postValue(null);
        postRepository.fetchPosts(sort, limit, offset, isPromptPost, new PostRepository.Callback<ApiService.PostsResponse>() {
            @Override
            public void onSuccess(ApiService.PostsResponse result) {
                loading.postValue(false);
                posts.postValue(result.posts != null ? result.posts : new ArrayList<>());
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
            }
        });
    }
}


