package com.example.navjit.konnect.model;

import java.io.Serializable;

public class ChatContact implements Serializable{
    private String firstName;
    private String lastName;
    private String threadId;
    private String lastMessage;

    public ChatContact(){}

    public ChatContact(String firstName, String lastName, String threadId, String lastMessage) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.threadId = threadId;
        this.lastMessage = lastMessage;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
