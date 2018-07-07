package com.example.navjit.konnect.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;

import com.example.navjit.konnect.R;
import com.example.navjit.konnect.model.ChatItemClickListener;
import com.example.navjit.konnect.model.ChatThread;
import com.example.navjit.konnect.model.ChatUser;
import com.example.navjit.konnect.model.NewChatAdapter;
import com.example.navjit.konnect.model.NewChatEngine;

import java.util.ArrayList;

public class NewChat extends AppCompatActivity {

    ArrayList<ChatUser> users=new ArrayList<>();
    ArrayList<ChatUser> otherUsers = new ArrayList<>();
    ArrayList<ChatUser> userToDisplay = new ArrayList<>();
    ChatUser currentUser;
    NewChatEngine engine;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    EditText editTextSearchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);
        recyclerView = findViewById(R.id.recyclerView);
        editTextSearchBar = findViewById(R.id.editTextSearchBar);
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            users = (ArrayList<ChatUser>) getIntent().getSerializableExtra("AllUsers");
            otherUsers = (ArrayList<ChatUser>) getIntent().getSerializableExtra("OtherUsers");
            currentUser = (ChatUser) getIntent().getSerializableExtra("Current User");
        }
        for(ChatUser u : users){
            for(ChatUser user : otherUsers){
                if(!user.getUserName().equals(u.getUserName())){
                    userToDisplay.add(u);
                }
            }
        }

        engine = new NewChatEngine(currentUser,userToDisplay);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new NewChatAdapter(engine, new ChatItemClickListener() {
            @Override
            public void onChatClickListener(ChatUser user, ChatThread chatThread) {
            }

            @Override
            public void onNewChatClickListener(ChatUser user) {
//                Intent contactIntent =  new Intent(getApplicationContext(),MainActivity.class);
//                contactIntent.putExtra("Username",user.getUserName());
//                contactIntent.putExtra("Name", user.getFirstName() + " " + user.getLastName());
//                contactIntent.putExtra("Thread", chatThread.getThreadId());
//                startActivity(contactIntent);
            }
        });

    }
}
