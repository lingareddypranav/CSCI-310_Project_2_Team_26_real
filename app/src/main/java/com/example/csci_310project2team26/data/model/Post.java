package com.example.csci_310project2team26.data.model;

public class Post {
    private String id;
    private String author_id;
    private String author_name;
    private String title;
    private String content;
    private String llm_tag;
    private boolean is_prompt_post;
    private String created_at;
    private String updated_at;
    private int upvotes;
    private int downvotes;
    private int comment_count;

    public String getId() { return id; }
    public String getAuthor_id() { return author_id; }
    public String getAuthor_name() { return author_name; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getLlm_tag() { return llm_tag; }
    public boolean isIs_prompt_post() { return is_prompt_post; }
    public String getCreated_at() { return created_at; }
    public String getUpdated_at() { return updated_at; }
    public int getUpvotes() { return upvotes; }
    public int getDownvotes() { return downvotes; }
    public int getComment_count() { return comment_count; }
}


