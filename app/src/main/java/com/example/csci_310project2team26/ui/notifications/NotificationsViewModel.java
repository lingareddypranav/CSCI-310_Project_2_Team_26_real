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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationsViewModel extends ViewModel {

    private final PostRepository postRepository = new PostRepository();
    private final CommentRepository commentRepository = new CommentRepository();

    private final Map<String, String> postTitleCache = new ConcurrentHashMap<>();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<List<UserActivityItem>> activityItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> successMessage = new MutableLiveData<>(null);

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<List<UserActivityItem>> getActivityItems() {
        return activityItems;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public void clearSuccessMessage() {
        successMessage.postValue(null);
    }

    public void loadUserActivity(String userId) {
        if (TextUtils.isEmpty(userId)) {
            activityItems.postValue(new ArrayList<>());
            return;
        }

        loading.postValue(true);
        error.postValue(null);
        successMessage.postValue(null);
        activityItems.postValue(new ArrayList<>());

        postRepository.fetchPostsForUser(userId, new PostRepository.Callback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> posts) {
                commentRepository.fetchCommentsByUser(userId, new CommentRepository.Callback<List<Comment>>() {
                    @Override
                    public void onSuccess(List<Comment> comments) {
                        loading.postValue(false);
                        activityItems.postValue(buildItems(posts, comments));
                        fetchMissingPostTitles(comments);
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

    public void deletePost(String postId) {
        if (TextUtils.isEmpty(postId)) {
            error.postValue("Post ID missing");
            return;
        }

        loading.postValue(true);
        postRepository.deletePost(postId, new PostRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                removeActivityItem(UserActivityItem.Type.POST, postId);
                successMessage.postValue("Post deleted");
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err != null ? err : "Failed to delete post");
            }
        });
    }

    public void deleteComment(String commentId) {
        if (TextUtils.isEmpty(commentId)) {
            error.postValue("Comment ID missing");
            return;
        }

        loading.postValue(true);
        commentRepository.deleteComment(commentId, new CommentRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                removeActivityItem(UserActivityItem.Type.COMMENT, commentId);
                successMessage.postValue("Comment deleted");
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err != null ? err : "Failed to delete comment");
            }
        });
    }

    private void removeActivityItem(UserActivityItem.Type type, String id) {
        List<UserActivityItem> current = activityItems.getValue();
        if (current == null || current.isEmpty()) {
            return;
        }

        List<UserActivityItem> updated = new ArrayList<>();
        for (UserActivityItem item : current) {
            if (item == null) continue;
            if (item.getType() == type && id.equals(item.getId())) {
                continue;
            }
            updated.add(item);
        }
        activityItems.postValue(updated);
    }

    private List<UserActivityItem> buildItems(List<Post> posts, List<Comment> comments) {
        List<UserActivityItem> items = new ArrayList<>();
        long now = System.currentTimeMillis();

        if (posts != null) {
            for (Post post : posts) {
                long created = parseTimestamp(post.getCreated_at());
                String tag = !TextUtils.isEmpty(post.getLlm_tag())
                        ? String.format(Locale.getDefault(), "#%s", post.getLlm_tag())
                        : "";
                String detail = !TextUtils.isEmpty(tag) ? tag.toUpperCase(Locale.getDefault()) : "";
                String title = !TextUtils.isEmpty(post.getTitle()) ? post.getTitle() : "(untitled post)";
                if (!TextUtils.isEmpty(post.getId())) {
                    postTitleCache.put(post.getId(), title);
                }
                items.add(new UserActivityItem(
                        UserActivityItem.Type.POST,
                        post.getId(),
                        post.getId(),
                        title,
                        detail,
                        created > 0 ? created : now
                ));
            }
        }

        if (comments != null) {
            for (Comment comment : comments) {
                long updated = parseTimestamp(comment.getCreated_at());
                // Use title if available, otherwise use text
                String title = !TextUtils.isEmpty(comment.getTitle())
                        ? comment.getTitle()
                        : (!TextUtils.isEmpty(comment.getText())
                                ? truncate(comment.getText(), 80)
                                : "(comment)");
                String postTitle = lookupPostTitle(comment.getPost_id());
                String detail = !TextUtils.isEmpty(comment.getPost_id())
                        ? String.format(Locale.getDefault(), "Post: %s", postTitle)
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
            // Try parsing as ISO 8601 date string
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
            format.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date date = format.parse(value);
            if (date != null) {
                return date.getTime();
            }
        } catch (Exception e) {
            // Try parsing as long timestamp
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e2) {
                return 0L;
            }
        }
        return 0L;
    }

    private String lookupPostTitle(String postId) {
        if (TextUtils.isEmpty(postId)) {
            return "";
        }
        String cached = postTitleCache.get(postId);
        if (!TextUtils.isEmpty(cached)) {
            return cached;
        }
        return postId;
    }

    private void fetchMissingPostTitles(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        Map<String, Boolean> requested = new HashMap<>();
        for (Comment comment : comments) {
            if (comment == null || TextUtils.isEmpty(comment.getPost_id())) continue;
            if (postTitleCache.containsKey(comment.getPost_id())) continue;
            if (requested.containsKey(comment.getPost_id())) continue;
            requested.put(comment.getPost_id(), true);

            postRepository.getPostById(comment.getPost_id(), new PostRepository.Callback<Post>() {
                @Override
                public void onSuccess(Post result) {
                    if (result == null || TextUtils.isEmpty(result.getId())) {
                        return;
                    }
                    String title = !TextUtils.isEmpty(result.getTitle()) ? result.getTitle() : result.getId();
                    postTitleCache.put(result.getId(), title);
                    updateCommentTitles(result.getId(), title);
                }

                @Override
                public void onError(String error) {
                    // Swallow errors to avoid interrupting UI; fallback remains post ID
                }
            });
        }
    }

    private void updateCommentTitles(String postId, String title) {
        List<UserActivityItem> current = activityItems.getValue();
        if (current == null || current.isEmpty()) {
            return;
        }
        List<UserActivityItem> updated = new ArrayList<>();
        for (UserActivityItem item : current) {
            if (item == null) continue;
            if (item.getType() == UserActivityItem.Type.COMMENT && postId.equals(item.getPostId())) {
                updated.add(new UserActivityItem(
                        item.getType(),
                        item.getId(),
                        item.getPostId(),
                        item.getTitle(),
                        String.format(Locale.getDefault(), "Post: %s", title),
                        item.getTimestamp()
                ));
            } else {
                updated.add(item);
            }
        }
        activityItems.postValue(updated);
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
