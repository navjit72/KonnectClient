package com.example.navjit.konnect.model;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.navjit.konnect.R;

import java.util.List;

public class NewChatAdapter extends RecyclerView.Adapter<NewChatAdapter.MyViewHolder>  {

    private NewChatEngine newChatEngine;
    private final ChatItemClickListener listener;

    public NewChatAdapter(NewChatEngine newChatEngine, ChatItemClickListener listener) {
        this.newChatEngine = newChatEngine;
        this.listener = listener;
    }

    @Override
    public NewChatAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item,parent,false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        List<ChatUser> users = newChatEngine.getNewChatUsers();
        ChatUser user=new ChatUser();
        for (ChatUser u: users) {
            user=u;
        }
        holder.name.setText(user.getFirstName() + " " + user.getLastName());
        holder.username.setText(user.getUserName());
        holder.bind(user,listener);
    }

    @Override
    public int getItemCount() {
        return 0;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView username;
        View itemView;

        public MyViewHolder(View view) {
            super(view);
            itemView=view;
            name = view.findViewById(R.id.textViewName);
            username = view.findViewById(R.id.textViewUsername);
        }

        public void bind(final ChatUser user, final ChatItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onNewChatClickListener(user);
                }
            });
        }
    }
}
