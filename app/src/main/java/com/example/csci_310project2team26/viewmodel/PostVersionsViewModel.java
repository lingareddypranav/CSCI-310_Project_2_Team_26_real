package com.example.csci_310project2team26.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.model.PostVersion;
import com.example.csci_310project2team26.data.repository.VersionRepository;

import java.util.ArrayList;
import java.util.List;

public class PostVersionsViewModel extends ViewModel {

    private final VersionRepository versionRepository = new VersionRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<List<PostVersion>> versions = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Post> revertedPost = new MutableLiveData<>(null);

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<List<PostVersion>> getVersions() {
        return versions;
    }

    public LiveData<Post> getRevertedPost() {
        return revertedPost;
    }

    public void loadVersions(String postId) {
        loading.postValue(true);
        error.postValue(null);

        versionRepository.getPostVersions(postId, new VersionRepository.Callback<List<PostVersion>>() {
            @Override
            public void onSuccess(List<PostVersion> result) {
                loading.postValue(false);
                versions.postValue(result != null ? result : new ArrayList<>());
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
            }
        });
    }

    public void revertToVersion(String postId, String versionId) {
        loading.postValue(true);
        error.postValue(null);
        revertedPost.postValue(null);

        versionRepository.revertToVersion(postId, versionId, new VersionRepository.Callback<Post>() {
            @Override
            public void onSuccess(Post result) {
                loading.postValue(false);
                revertedPost.postValue(result);
                // Reload versions after revert
                loadVersions(postId);
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
            }
        });
    }
}

