package com.example.navjit.konnect.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ChatEngine {
    private ChatUser user;
    private List<ChatContact> chatContacts;
   // private List<FriendlyMessage> messageList;

    public ChatEngine(ChatUser user,List<ChatContact> contactList){
        this.user = user;
        chatContacts = contactList;
//        secondUsers=secondUsersList;
//        chatThreads=threadList;
        //messageList=messages;
    }
    public ChatEngine(){
    }


    public List<ChatContact> getChatContacts() {
        return chatContacts;
    }


    public ChatContact getChatContact(int index){
        return chatContacts.get(index);
    }

    public ChatUser getUser() {
        return user;
    }

}
