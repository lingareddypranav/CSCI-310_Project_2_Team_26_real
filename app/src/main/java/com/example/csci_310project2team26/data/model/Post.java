package com.example.csci_310project2team26.data.model;

import com.google.gson.annotations.SerializedName;

public class Post {
    @SerializedName("id")
    private String id;
    
    @SerializedName("author_id")
    private String author_id;
    
    @SerializedName("author_name")
    private String author_name;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("content")
    private String content;
    
    @SerializedName("prompt_section")
    private String prompt_section;
    
    @SerializedName("description_section")
    private String description_section;
    
    @SerializedName("llm_tag")
    private String llm_tag;
    
    @SerializedName("is_prompt_post")
    private boolean is_prompt_post;
    
    @SerializedName("created_at")
    private String created_at;
    
    @SerializedName("updated_at")
    private String updated_at;

    @SerializedName("anonymous")
    private boolean anonymous;
    
    @SerializedName("upvotes")
    private int upvotes;
    
    @SerializedName("downvotes")
    private int downvotes;
    
    @SerializedName("comment_count")
    private int comment_count;

    public Post() {}

    public Post(String id,
                String authorId,
                String authorName,
                String title,
                String content,
                String llmTag,
                boolean isPromptPost,
                boolean anonymous,
                String createdAt,
                String updatedAt,
                int upvotes,
                int downvotes,
                int commentCount) {
        this.id = id;
        this.author_id = authorId;
        this.author_name = authorName;
        this.title = title;
        this.content = content;
        this.llm_tag = llmTag;
        this.is_prompt_post = isPromptPost;
        this.anonymous = anonymous;
        this.created_at = createdAt;
        this.updated_at = updatedAt;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.comment_count = commentCount;
    }

    public String getId() { return id; }
    public String getAuthor_id() { return author_id; }
    public String getAuthor_name() { return author_name; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getPrompt_section() { return prompt_section; }
    public String getDescription_section() { return description_section; }
    public String getLlm_tag() { return llm_tag; }
    public boolean isIs_prompt_post() { return is_prompt_post; }
    public boolean isAnonymous() { return anonymous; }
    public String getCreated_at() { return created_at; }
    public String getUpdated_at() { return updated_at; }
    public int getUpvotes() { return upvotes; }
    public int getDownvotes() { return downvotes; }
    public int getComment_count() { return comment_count; }

    public void setId(String id) { this.id = id; }
    public void setAuthor_id(String author_id) { this.author_id = author_id; }
    public void setAuthor_name(String author_name) { this.author_name = author_name; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setPrompt_section(String prompt_section) { this.prompt_section = prompt_section; }
    public void setDescription_section(String description_section) { this.description_section = description_section; }
    public void setLlm_tag(String llm_tag) { this.llm_tag = llm_tag; }
    public void setIs_prompt_post(boolean is_prompt_post) { this.is_prompt_post = is_prompt_post; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
    public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }
    public void setDownvotes(int downvotes) { this.downvotes = downvotes; }
    public void setComment_count(int comment_count) { this.comment_count = comment_count; }
}