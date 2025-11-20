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
        String trimmedTitle = title != null ? title.trim() : "";
        String trimmedTag = tag != null ? tag.trim() : "";
        String trimmedContent = content != null ? content.trim() : "";
        String trimmedPrompt = promptSection != null ? promptSection.trim() : "";
        String trimmedDescription = descriptionSection != null ? descriptionSection.trim() : "";

        if (trimmedTitle.isEmpty()) {
            error.postValue("Title is required");
            return;
        }

        if (trimmedTag.isEmpty()) {
            error.postValue("Tag is required");
            return;
        }

        // Normalize prompt intent so stale toggle state or empty prompt fields don't misclassify
        // the submission as a prompt post.
        boolean hasPromptContent = !trimmedPrompt.isEmpty() || !trimmedDescription.isEmpty();
        boolean normalizedIsPrompt = isPrompt && hasPromptContent;

        // For prompt posts, require prompt text and description. For regular posts, require content.
        if (normalizedIsPrompt) {
            if (trimmedPrompt.isEmpty()) {
                error.postValue("Prompt text is required for prompt posts");
                return;
            }
            if (trimmedDescription.isEmpty()) {
                error.postValue("Description is required for prompt posts");
                return;
            }
        } else {
            if (trimmedContent.isEmpty()) {
                error.postValue("Content is required");
                return;
            }
        }

        loading.postValue(true);
        error.postValue(null);
        createdPost.postValue(null);

        postRepository.createPost(
                trimmedTitle,
                trimmedContent,
                trimmedTag,
                normalizedIsPrompt,
                normalizedIsPrompt ? trimmedPrompt : null,
                normalizedIsPrompt ? trimmedDescription : null,
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
