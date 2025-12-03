package com.example.csci_310project2team26.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.repository.PostRepository;

/**
 * PostDetailViewModel - Manages post detail state and voting
 * Similar to CommentsViewModel pattern
 */
public class PostDetailViewModel extends ViewModel {

    private final PostRepository postRepository = new PostRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<Post> post = new MutableLiveData<>(null);

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<Post> getPost() { return post; }

    public void loadPost(String postId) {
        loading.postValue(true);
        error.postValue(null);
        postRepository.getPostById(postId, new PostRepository.Callback<Post>() {
            @Override
            public void onSuccess(Post result) {
                loading.postValue(false);
                Post existing = post.getValue();
                if (result != null
                        && result.getUser_vote_type() == null
                        && existing != null
                        && existing.getUser_vote_type() != null) {
                    // Preserve the user's known vote selection when the backend response
                    // does not echo it back. This keeps the arrow fill state from
                    // clearing after a vote even if the server omits the user_vote_type.
                    result.setUser_vote_type(existing.getUser_vote_type());
                }
                post.postValue(result);
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
            }
        });
    }

    public void voteOnPost(String postId, String type) {
        if (postId == null || type == null) {
            error.postValue("Invalid vote parameters");
            return;
        }
        
        loading.postValue(true);
        error.postValue(null);
        
        postRepository.votePost(postId, type, new PostRepository.Callback<PostRepository.VoteActionResult>() {
            @Override
            public void onSuccess(PostRepository.VoteActionResult result) {
                // After voting, reload the post to get updated vote counts
                // This follows the same pattern as comment voting
                loadPost(postId);
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err != null ? err : "Failed to vote on post");
            }
        });
    }
}

