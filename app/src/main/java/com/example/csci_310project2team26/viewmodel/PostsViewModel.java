package com.example.csci_310project2team26.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * PostsViewModel - Exposes posts feed and actions.
 */
public class PostsViewModel extends ViewModel {

    public static final String SORT_NEW = "new";
    public static final String SORT_TOP = "top";
    private static final String DEFAULT_SEARCH_TYPE = "full_text";

    private final PostRepository postRepository = new PostRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<List<Post>> posts = new MutableLiveData<>(new ArrayList<>());

    private String currentSort = SORT_NEW;
    private String currentQuery = "";
    private Integer currentLimit = null;
    private Integer currentOffset = null;
    private Boolean currentIsPromptPost = null;

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<List<Post>> getPosts() { return posts; }

    public String getCurrentSort() {
        return currentSort;
    }

    public String getCurrentQuery() {
        return currentQuery;
    }

    public Integer getCurrentLimit() {
        return currentLimit;
    }

    public Integer getCurrentOffset() {
        return currentOffset;
    }

    public Boolean getCurrentIsPromptPost() {
        return currentIsPromptPost;
    }

    public void loadPosts(String sort,
                          String query,
                          Integer limit,
                          Integer offset,
                          Boolean isPromptPost) {
        currentSort = sort != null ? sort : SORT_NEW;
        currentQuery = query != null ? query.trim() : "";
        currentLimit = limit;
        currentOffset = offset;
        currentIsPromptPost = isPromptPost;

        loading.postValue(true);
        error.postValue(null);

        if (currentQuery.isEmpty()) {
            postRepository.fetchPosts(currentSort, currentLimit, currentOffset, currentIsPromptPost,
                    new PostRepository.Callback<PostRepository.PostsResult>() {
                        @Override
                        public void onSuccess(PostRepository.PostsResult result) {
                            loading.postValue(false);
                            posts.postValue(result != null && result.getPosts() != null
                                    ? result.getPosts() : new ArrayList<>());
                        }

                        @Override
                        public void onError(String err) {
                            loading.postValue(false);
                            error.postValue(err);
                        }
                    });
        } else {
            performSearch(currentQuery, DEFAULT_SEARCH_TYPE, currentSort, currentLimit, currentOffset, currentIsPromptPost);
        }
    }

    public void searchPosts(String query,
                            String searchType,
                            String sort,
                            Integer limit,
                            Integer offset,
                            Boolean isPromptPost) {
        currentSort = sort != null ? sort : SORT_NEW;
        currentQuery = query != null ? query.trim() : "";
        currentLimit = limit;
        currentOffset = offset;
        currentIsPromptPost = isPromptPost;

        performSearch(currentQuery,
                searchType != null ? searchType : DEFAULT_SEARCH_TYPE,
                currentSort,
                currentLimit,
                currentOffset,
                currentIsPromptPost);
    }

    private void performSearch(String query,
                               String searchType,
                               String sort,
                               Integer limit,
                               Integer offset,
                               Boolean isPromptPost) {
        loading.postValue(true);
        error.postValue(null);
        postRepository.searchPosts(query, searchType, sort, limit, offset, isPromptPost,
                new PostRepository.Callback<PostRepository.PostsResult>() {
                    @Override
                    public void onSuccess(PostRepository.PostsResult result) {
                        loading.postValue(false);
                        posts.postValue(result != null && result.getPosts() != null
                                ? result.getPosts() : new ArrayList<>());
                    }

                    @Override
                    public void onError(String err) {
                        loading.postValue(false);
                        error.postValue(err);
                    }
                });
    }
}