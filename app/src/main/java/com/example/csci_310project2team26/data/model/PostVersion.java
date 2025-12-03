package com.example.csci_310project2team26.data.model;

import com.google.gson.annotations.SerializedName;

public class PostVersion {
    @SerializedName("id")
    private String id;
    
    @SerializedName("post_id")
    private String post_id;
    
    @SerializedName("version_number")
    private int version_number;
    
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
    
    @SerializedName("anonymous")
    private boolean anonymous;
    
    @SerializedName("created_at")
    private String created_at;
    
    @SerializedName("created_by")
    private String created_by;

    public PostVersion() {}

    public String getId() { return id; }
    public String getPost_id() { return post_id; }
    public int getVersion_number() { return version_number; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getPrompt_section() { return prompt_section; }
    public String getDescription_section() { return description_section; }
    public String getLlm_tag() { return llm_tag; }
    public boolean isIs_prompt_post() { return is_prompt_post; }
    public boolean isAnonymous() { return anonymous; }
    public String getCreated_at() { return created_at; }
    public String getCreated_by() { return created_by; }

    public void setId(String id) { this.id = id; }
    public void setPost_id(String post_id) { this.post_id = post_id; }
    public void setVersion_number(int version_number) { this.version_number = version_number; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setPrompt_section(String prompt_section) { this.prompt_section = prompt_section; }
    public void setDescription_section(String description_section) { this.description_section = description_section; }
    public void setLlm_tag(String llm_tag) { this.llm_tag = llm_tag; }
    public void setIs_prompt_post(boolean is_prompt_post) { this.is_prompt_post = is_prompt_post; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
    public void setCreated_by(String created_by) { this.created_by = created_by; }
}

