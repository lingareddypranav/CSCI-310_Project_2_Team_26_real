package com.example.csci_310project2team26.data.model;

import com.google.gson.annotations.SerializedName;

public class Draft {
    @SerializedName("id")
    private String id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("content")
    private String body;
    
    @SerializedName("llm_tag")
    private String tag;
    
    @SerializedName("is_prompt_post")
    private boolean prompt;
    
    @SerializedName("prompt_section")
    private String promptSection;
    
    @SerializedName("description_section")
    private String descriptionSection;
    
    @SerializedName("anonymous")
    private boolean anonymous;
    
    @SerializedName("updated_at")
    private String updatedAt;

    public Draft() {}

    public Draft(String id,
                 String title,
                 String body,
                 String tag,
                 boolean prompt,
                 String promptSection,
                 String descriptionSection,
                 long updatedAt) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.tag = tag;
        this.prompt = prompt;
        this.promptSection = promptSection;
        this.descriptionSection = descriptionSection;
        this.updatedAt = String.valueOf(updatedAt);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getTag() {
        return tag;
    }

    public boolean isPrompt() {
        return prompt;
    }

    public String getPromptSection() {
        return promptSection;
    }

    public String getDescriptionSection() {
        return descriptionSection;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setBody(String body) { this.body = body; }
    public void setTag(String tag) { this.tag = tag; }
    public void setPrompt(boolean prompt) { this.prompt = prompt; }
    public void setPromptSection(String promptSection) { this.promptSection = promptSection; }
    public void setDescriptionSection(String descriptionSection) { this.descriptionSection = descriptionSection; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
