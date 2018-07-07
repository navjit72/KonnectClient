package com.example.navjit.konnect.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.example.navjit.konnect.model.ChatAdapter;
import com.example.navjit.konnect.model.ChatContact;
import com.example.navjit.konnect.model.ChatEngine;
import com.example.navjit.konnect.model.ChatItemClickListener;
import com.example.navjit.konnect.model.ChatThread;
import com.example.navjit.konnect.model.ChatUser;
import com.example.navjit.konnect.R;
import com.example.navjit.konnect.model.FriendlyMessage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatListActivity extends AppCompatActivity {

    private DatabaseReference mFirebaseDatabaseReference;
    ArrayList<ChatThread> chatThreadDetails = new ArrayList<>();
    ArrayList<ChatUser> users = new ArrayList<>();
    ArrayList<ChatUser> secondUsers = new ArrayList<>();
    ArrayList<FriendlyMessage> friendlyMessageList= new ArrayList<>();
    ArrayList<ChatContact> contactList = new ArrayList<>();


    ChatUser userOne = new ChatUser();

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    ChatEngine engine;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        Bundle bundle = getIntent().getExtras();
        if(bundle !=null) {
            userOne = (ChatUser)getIntent().getSerializableExtra("Current User");
//            userOne.setUserName(bundle.getString("Username"));
//            userOne.setFirstName(bundle.getString("FirstName"));
//            userOne.setLastName(bundle.getString("LastName"));
//            userOne.setUserType(bundle.getString("UserType"));
        }

        mFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot userSnap = dataSnapshot.child("login");
                Iterable<DataSnapshot> userDetails = userSnap.getChildren();
                for(DataSnapshot s : userDetails)
                {
                    ChatUser user = s.getValue(ChatUser.class);
                    users.add(user);
                }

                DataSnapshot threadSnap = dataSnapshot.child("thread");
                Iterable<DataSnapshot> threadDetails = threadSnap.getChildren();

                for (DataSnapshot snap : threadDetails) {
                    ChatThread chatThread = snap.getValue(ChatThread.class);
                    //Log.d("ChatThread", "ChatThread threadId : " + chatThread.getThreadId() + "Chat User : " + chatThread.getMessengerOne());
                    if(userOne.getUserName().equals(chatThread.getMessengerOne()) || userOne.getUserName().equals(chatThread.getMessengerTwo())) {
                        chatThreadDetails.add(chatThread);
                    }
                }
                for(ChatThread t : chatThreadDetails)
                {
                    String secondUsername = "";
                    if(t.getMessengerOne().equals(userOne.getUserName())) {
                        secondUsername =t.getMessengerTwo();
                    }
                    else {
                        secondUsername = t.getMessengerOne();
                    }
                    for(ChatUser u : users)
                    {
                        if(u.getUserName().equals(secondUsername))
                            secondUsers.add(u);
                    }
                }
//                for (ChatThread t: chatThreadDetails) {
//                    Log.d("Chat thread details","Thread  :" + t.getThreadId() + t.getMessengerOne() + t.getMessengerTwo());
//                }
                FriendlyMessage friendlyMessage=null;
                for(ChatThread chatThread : chatThreadDetails) {
                    DataSnapshot messagesSnap = dataSnapshot.child(chatThread.getThreadId());
                    Iterable<DataSnapshot> lastmsgDetails = messagesSnap.getChildren();
                    for(DataSnapshot snap : lastmsgDetails){
                        friendlyMessage = snap.getValue(FriendlyMessage.class);
                    }
                    friendlyMessageList.add(friendlyMessage);
                    Log.d("Friendly Message","Friendly Message " + friendlyMessage.getThreadId() + " : " + friendlyMessage.getText() + friendlyMessage.getName());
                }
                for(int i=0;i<friendlyMessageList.size();i++)
                {
                    ChatContact contact = new ChatContact();
                    contact.setFirstName(secondUsers.get(i).getFirstName());
                    contact.setLastName(secondUsers.get(i).getLastName());
                    contact.setThreadId(friendlyMessageList.get(i).getThreadId());
                    contact.setLastMessage(friendlyMessageList.get(i).getText());
                    contact.setUserName(secondUsers.get(i).getUserName());
                    contactList.add(contact);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newChatIntent = new Intent(getApplicationContext(),NewChat.class);
                newChatIntent.putExtra("AllUsers", users);
                newChatIntent.putExtra("OtherUsers",secondUsers);
                newChatIntent.putExtra("Current User",userOne);
                startActivity(newChatIntent);
            }
        });

        engine=new ChatEngine(userOne,contactList);
        recyclerView = findViewById(R.id.recyclerViewChatList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new ChatAdapter(engine, new ChatItemClickListener() {
            @Override
            public void onChatClickListener(ChatContact contact) {
                Intent contactIntent =  new Intent(getApplicationContext(),MainActivity.class);
//                contactIntent.putExtra("Username",user.getUserName());
//                contactIntent.putExtra("Name", user.getFirstName() + " " + user.getLastName());
                contactIntent.putExtra("Current User",userOne);
                contactIntent.putExtra("Other UserName",contact.getUserName());
                contactIntent.putExtra("Other User FirstName",contact.getFirstName());
                contactIntent.putExtra("Other User LastName",contact.getLastName());
                contactIntent.putExtra("Thread", contact.getThreadId());
                startActivity(contactIntent);
            }

            @Override
            public void onNewChatClickListener(ChatUser user) {
            }
        });
        recyclerView.setAdapter(adapter);
    }

}
