package com.example.csci_310project2team26.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.repository.PostRepository;

public class CreatePostViewModel extends ViewModel {

    private final PostRepository postRepository = new PostRepository();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<Post> createdPost = new MutableLiveData<>(null);

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Post> getCreatedPost() {
        return createdPost;
    }

    public void createPost(String title, String content, String tag, boolean isPrompt, 
                           String promptSection, String descriptionSection) {
        if (title == null || title.trim().isEmpty()) {
            error.postValue("Title is required");
            return;
        }
        
        if (tag == null || tag.trim().isEmpty()) {
            error.postValue("Tag is required");
            return;
        }

        // For prompt posts, require prompt text and description. For regular posts, require content.
        if (isPrompt) {
            if (promptSection == null || promptSection.trim().isEmpty()) {
                error.postValue("Prompt text is required for prompt posts");
                return;
            }
            if (descriptionSection == null || descriptionSection.trim().isEmpty()) {
                error.postValue("Description is required for prompt posts");
                return;
            }
        } else {
            if (content == null || content.trim().isEmpty()) {
                error.postValue("Content is required");
                return;
            }
        }

        loading.postValue(true);
        error.postValue(null);
        createdPost.postValue(null);

        postRepository.createPost(
                title.trim(), 
                content != null ? content.trim() : "", 
                tag != null ? tag.trim() : "", 
                isPrompt,
                promptSection != null ? promptSection.trim() : null,
                descriptionSection != null ? descriptionSection.trim() : null,
                new PostRepository.Callback<Post>() {
                    @Override
                    public void onSuccess(Post result) {
                        loading.postValue(false);
                        createdPost.postValue(result);
                    }

                    @Override
                    public void onError(String err) {
                        loading.postValue(false);
                        error.postValue(err);
                    }
                });
    }
}
