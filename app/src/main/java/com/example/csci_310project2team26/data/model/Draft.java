package com.example.csci_310project2team26.data.model;

public class Draft {
    private final String id;
    private final String title;
    private final String body;
    private final String tag;
    private final boolean prompt;
    private final String promptSection;
    private final String descriptionSection;
    private final long updatedAt;

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
        this.updatedAt = updatedAt;
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

    public long getUpdatedAt() {
        return updatedAt;
    }
}
