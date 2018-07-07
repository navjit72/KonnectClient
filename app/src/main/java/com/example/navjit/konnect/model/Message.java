package com.example.navjit.konnect.model;

public class Message {

    private String id;
    private String text;
    private String name;
    private String photoUrl;
    private String imageUrl;
    private String threadId;

    public Message() {

    }

    public Message(String text, String name, String photoUrl, String imageUrl, String threadId) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.imageUrl = imageUrl;
        this.threadId=threadId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getText() {
        return text;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getThreadId() { return threadId; }

    public void setThreadId(String threadId) { this.threadId = threadId; }
}
