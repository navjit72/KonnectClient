/***************************************************************************
 *     Author : Navjit Kaur
 *
 *     This entity class is to load the recycler view in chat list activity.
 ***************************************************************************/

package com.example.navjit.konnect.model;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.navjit.konnect.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    private ChatEngine chatEngine;
    private final ChatItemClickListener listener;

    public ChatAdapter(ChatEngine engine, ChatItemClickListener chatItemClickListener)
    {
        chatEngine=engine;
        listener=chatItemClickListener;
    }

    //create a new view
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item,parent,false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;

    }

    //get element from the chat engine at that position and replace the content of view with that element
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ChatContact contact =chatEngine.getChatContact(position);
        holder.name.setText(contact.getFirstName() + " " + contact.getLastName());
        holder.lastMsg.setText(contact.getLastMessage());
        holder.bind(contact,listener);

    }

    //returning size of dataset from chat engine
    @Override
    public int getItemCount() {
        return chatEngine.getChatContacts().size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView lastMsg;
        View itemView;

        public MyViewHolder(View view) {
            super(view);
            itemView=view;
            name = view.findViewById(R.id.textViewName);
            lastMsg = view.findViewById(R.id.textViewLastMsg);
        }

        //binding the listener to move on to main activity for the user item clicked.
        public void bind(final ChatContact chatContact, final ChatItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onChatClickListener(chatContact);
                }
            });
        }
    }
}
