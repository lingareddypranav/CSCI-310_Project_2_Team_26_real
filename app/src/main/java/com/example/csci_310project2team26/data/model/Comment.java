package com.example.csci_310project2team26.data.model;

public class Comment {
    private String id;
    private String post_id;
    private String author_id;
    private String author_name;
    private String text;
    private String created_at;
    private String updated_at;
    private int upvotes;
    private int downvotes;

    public String getId() { return id; }
    public String getPost_id() { return post_id; }
    public String getAuthor_id() { return author_id; }
    public String getAuthor_name() { return author_name; }
    public String getText() { return text; }
    public String getCreated_at() { return created_at; }
    public String getUpdated_at() { return updated_at; }
    public int getUpvotes() { return upvotes; }
    public int getDownvotes() { return downvotes; }
}


