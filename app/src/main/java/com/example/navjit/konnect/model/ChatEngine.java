/*********************************************************
 *     Author : Navjit Kaur
 *
 *     This entity class is to provide the list of users
 *     available to chat and current user.
 **********************************************************/

package com.example.navjit.konnect.model;

import java.util.List;

public class ChatEngine {
    private ChatUser user;
    private List<ChatContact> chatContacts;

    public ChatEngine(ChatUser user,List<ChatContact> contactList){
        this.user = user;
        chatContacts = contactList;
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
