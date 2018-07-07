package com.example.navjit.konnect.model;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item,parent,false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        List<ChatUser> otherUsers = chatEngine.getSecondUsers();
        ChatUser otherUser = new ChatUser();
        ChatUser user = chatEngine.getUser();
       // Log.d("Chat engine user","user : " + user.getFirstName());
        ChatThread chatThread =chatEngine.getChatThread(position);
        //FriendlyMessage message = chatEngine.getFriendlyMessage(position);
        //Log.d("Adapter message","Adapter message" + message.getThreadId() + " : " + message.getText());
        if(user.getUserName().equals(chatThread.getMessengerOne())) {
            for (ChatUser u : otherUsers) {
                if (u.getUserName().equals(chatThread.getMessengerTwo()))
                    otherUser=u;
            }
        }
        else {
            for (ChatUser u :otherUsers) {
                if(u.getUserName().equals(chatThread.getMessengerOne()))
                    otherUser=u;
            }
        }
        holder.name.setText(otherUser.getFirstName() + " " + otherUser.getLastName());
        holder.lastMsg.setText(chatThread.getThreadId());
        holder.bind(user,chatThread,listener);

    }

    @Override
    public int getItemCount() {
        return chatEngine.getChatThreads().size();
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

        public void bind(final ChatUser user,final ChatThread thread, final ChatItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onChatClickListener(user,thread);
                }
            });
        }
    }
}
