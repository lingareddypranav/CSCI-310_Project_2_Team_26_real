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
    private final boolean promptPost;

    public UserActivityItem(Type type, String id, String postId, String title, String subtitle, long timestamp, boolean promptPost) {
        this.type = type;
        this.id = id;
        this.postId = postId;
        this.title = title;
        this.subtitle = subtitle;
        this.timestamp = timestamp;
        this.promptPost = promptPost;
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

    public boolean isPromptPost() {
        return promptPost;
    }
}
