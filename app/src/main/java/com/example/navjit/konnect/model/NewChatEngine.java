package com.example.navjit.konnect.model;

import java.util.List;

public class NewChatEngine{
    private ChatUser userOne;
    private List<ChatUser> newChatUsers;

    public NewChatEngine(ChatUser user,List<ChatUser> newChatUsers) {
        userOne=user;
        this.newChatUsers = newChatUsers;
    }

    public NewChatEngine(){}

    public ChatUser getUserOne() {
        return userOne;
    }

    public ChatUser getNewChatUser(int position){
        return newChatUsers.get(position);
    }

    public void setUserOne(ChatUser userOne) {
        this.userOne = userOne;
    }

    public List<ChatUser> getNewChatUsers() {
        return newChatUsers;
    }

    public void setNewChatUsers(List<ChatUser> newChatUsers) {
        this.newChatUsers = newChatUsers;
    }
}
