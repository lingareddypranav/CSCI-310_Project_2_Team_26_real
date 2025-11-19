package com.example.csci_310project2team26.ui.notifications;

public class UserActivityItem {

    public enum Type {
        POST,
        COMMENT
    }

    private final Type type;
    private final String id;
    private final String postId;
    private final String title;
    private final String subtitle;
    private final long timestamp;

    public UserActivityItem(Type type, String id, String postId, String title, String subtitle, long timestamp) {
        this.type = type;
        this.id = id;
        this.postId = postId;
        this.title = title;
        this.subtitle = subtitle;
        this.timestamp = timestamp;
    }

    public Type getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getPostId() {
        return postId;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
