package com.example.navjit.konnect.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ChatEngine {
    private ChatUser user;
    private List<ChatUser> secondUsers;
    private List<ChatThread> chatThreads;

    public ChatEngine(ChatUser user,List<ChatUser> secondUsersList,List<ChatThread> threadList){
        this.user = user;
        secondUsers=secondUsersList;
        chatThreads=threadList;
    }
    public ChatEngine(){
    }

    public List<ChatThread> getChatThreads() {
        return chatThreads;
    }


    public ChatThread getChatThread(int index){
        return chatThreads.get(index);
    }

    public ChatUser getUser() {
        return user;
    }

    public List<ChatUser> getSecondUsers() {
        return secondUsers;
    }
}