package com.example.navjit.konnect.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.example.navjit.konnect.model.ChatThread;
import com.example.navjit.konnect.model.ChatUser;
import com.example.navjit.konnect.R;
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

    ChatUser userOne = new ChatUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        Bundle bundle = getIntent().getExtras();
        if(bundle !=null) {
            userOne.setUserName(bundle.getString("Username"));
            userOne.setFirstName(bundle.getString("FirstName"));
            userOne.setLastName(bundle.getString("LastName"));
            userOne.setUserType(bundle.getString("UserType"));
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
                    Log.d("ChatThread", "ChatThread threadId : " + chatThread.getThreadId() + "Chat User : " + chatThread.getMessengerOne());
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
                for(ChatUser u : secondUsers)
                {
                    Log.d("Second User", "Second user : " + u.getFirstName() + " " + u.getLastName());
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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
