/**************************************************************
 *     Author : Navjit Kaur
 *
 *     This interface is to provide click listeners on items
 *     in new chat activity and chat list activity.
 ***************************************************************/

package com.example.navjit.konnect.model;

public interface ChatItemClickListener {
    //listener for chats already present in chat list activity
    void onChatClickListener(ChatContact contact);

    //listener for new chat being created from new chat activity
    void onNewChatClickListener(ChatUser user);
}
