/***************************************************************************
 *     Author : Navjit Kaur
 *
 *     This entity class is to load the recycler view in new chat activity.
 ***************************************************************************/

package com.example.navjit.konnect.model;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.navjit.konnect.R;

public class NewChatAdapter extends RecyclerView.Adapter<NewChatAdapter.MyViewHolder>  {

    private NewChatEngine newChatEngine;
    private final ChatItemClickListener listener;

    public NewChatAdapter(NewChatEngine newChatEngine, ChatItemClickListener listener) {
        this.newChatEngine = newChatEngine;
        this.listener = listener;
    }

    //create a new view
    @Override
    public NewChatAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_chat_users_item,parent,false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    //get element from the new chat engine at that position and replace the content of view with that element
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ChatUser user = newChatEngine.getNewChatUser(position);
        holder.name.setText(user.getFirstName() + " " + user.getLastName());
        holder.username.setText(user.getUserName());
        holder.bind(user,listener);
    }

    //get the size of dataset from new chat engine
    @Override
    public int getItemCount() {
        return newChatEngine.getNewChatUsers().size();
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

        //binding the listener to move on to main activity for the user item clicked.
        public void bind(final ChatUser user, final ChatItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onNewChatClickListener(user);
                }
            });
        }
    }
}
