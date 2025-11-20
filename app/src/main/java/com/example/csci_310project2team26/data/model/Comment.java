package com.example.csci_310project2team26.data.model;

import com.google.gson.annotations.SerializedName;

public class Comment {
    @SerializedName("id")
    private String id;
    
    @SerializedName("post_id")
    private String post_id;
    
    @SerializedName("author_id")
    private String author_id;
    
    @SerializedName("author_name")
    private String author_name;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("text")
    private String text;
    
    @SerializedName("created_at")
    private String created_at;
    
    @SerializedName("updated_at")
    private String updated_at;
    
    @SerializedName("upvotes")
    private int upvotes;
    
    @SerializedName("downvotes")
    private int downvotes;

    public Comment() {}

    public Comment(String id,
                   String postId,
                   String authorId,
                   String authorName,
                   String text,
                   String createdAt,
                   String updatedAt,
                   int upvotes,
                   int downvotes) {
        this.id = id;
        this.post_id = postId;
        this.author_id = authorId;
        this.author_name = authorName;
        this.text = text;
        this.created_at = createdAt;
        this.updated_at = updatedAt;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
    }

    public String getId() { return id; }
    public String getPost_id() { return post_id; }
    public String getAuthor_id() { return author_id; }
    public String getAuthor_name() { return author_name; }
    public String getTitle() { return title; }
    public String getText() { return text; }
    public String getCreated_at() { return created_at; }
    public String getUpdated_at() { return updated_at; }
    public int getUpvotes() { return upvotes; }
    public int getDownvotes() { return downvotes; }

    public void setId(String id) { this.id = id; }
    public void setPost_id(String post_id) { this.post_id = post_id; }
    public void setAuthor_id(String author_id) { this.author_id = author_id; }
    public void setAuthor_name(String author_name) { this.author_name = author_name; }
    public void setTitle(String title) { this.title = title; }
    public void setText(String text) { this.text = text; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
    public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }
    public void setDownvotes(int downvotes) { this.downvotes = downvotes; }
}


