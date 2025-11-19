package com.example.csci_310project2team26.ui.notifications;

import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.csci_310project2team26.data.model.Comment;
import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.repository.CommentRepository;
import com.example.csci_310project2team26.data.repository.PostRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class NotificationsViewModel extends ViewModel {

    private final PostRepository postRepository = new PostRepository();
    private final CommentRepository commentRepository = new CommentRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<List<UserActivityItem>> activityItems = new MutableLiveData<>(new ArrayList<>());

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<List<UserActivityItem>> getActivityItems() {
        return activityItems;
    }

    public void loadUserActivity(String userId) {
        if (TextUtils.isEmpty(userId)) {
            activityItems.postValue(new ArrayList<>());
            return;
        }

        loading.postValue(true);
        error.postValue(null);
        activityItems.postValue(new ArrayList<>());

        postRepository.fetchPostsForUser(userId, new PostRepository.Callback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> posts) {
                commentRepository.fetchCommentsByUser(userId, new CommentRepository.Callback<List<Comment>>() {
                    @Override
                    public void onSuccess(List<Comment> comments) {
                        loading.postValue(false);
                        activityItems.postValue(buildItems(posts, comments));
                    }

                    @Override
                    public void onError(String err) {
                        loading.postValue(false);
                        error.postValue(err);
                    }
                });
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
            }
        });
    }

    private List<UserActivityItem> buildItems(List<Post> posts, List<Comment> comments) {
        List<UserActivityItem> items = new ArrayList<>();
        long now = System.currentTimeMillis();

        if (posts != null) {
            for (Post post : posts) {
                long updated = parseTimestamp(post.getUpdated_at());
                String tag = !TextUtils.isEmpty(post.getLlm_tag())
                        ? String.format(Locale.getDefault(), "#%s", post.getLlm_tag())
                        : "";
                String detail = !TextUtils.isEmpty(tag) ? tag.toUpperCase(Locale.getDefault()) : "";
                String title = !TextUtils.isEmpty(post.getTitle()) ? post.getTitle() : "(untitled post)";
                items.add(new UserActivityItem(
                        UserActivityItem.Type.POST,
                        post.getId(),
                        post.getId(),
                        title,
                        detail,
                        updated > 0 ? updated : now
                ));
            }
        }

        if (comments != null) {
            for (Comment comment : comments) {
                long updated = parseTimestamp(comment.getUpdated_at());
                String title = !TextUtils.isEmpty(comment.getText())
                        ? truncate(comment.getText(), 80)
                        : "(comment)";
                String detail = !TextUtils.isEmpty(comment.getPost_id())
                        ? String.format(Locale.getDefault(), "Post: %s", comment.getPost_id())
                        : "";
                items.add(new UserActivityItem(
                        UserActivityItem.Type.COMMENT,
                        comment.getId(),
                        comment.getPost_id(),
                        title,
                        detail,
                        updated > 0 ? updated : now
                ));
            }
        }

        Collections.sort(items, Comparator.comparingLong(UserActivityItem::getTimestamp).reversed());
        return items;
    }

    private long parseTimestamp(String value) {
        if (TextUtils.isEmpty(value)) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private String truncate(String value, int maxLength) {
        if (TextUtils.isEmpty(value)) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength - 1) + "\u2026";
    }
}
