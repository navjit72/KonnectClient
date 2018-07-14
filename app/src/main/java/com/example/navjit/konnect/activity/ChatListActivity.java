package com.example.navjit.konnect.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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
import com.google.gson.Gson;

import java.util.ArrayList;

public class ChatListActivity extends AppCompatActivity {

    private DatabaseReference mFirebaseDatabaseReference;
    ArrayList<ChatThread> chatThreadDetails = new ArrayList<>();
    ArrayList<ChatUser> users = new ArrayList<>();
    ArrayList<ChatUser> secondUsers = new ArrayList<>();
    ArrayList<FriendlyMessage> friendlyMessageList = new ArrayList<>();
    ArrayList<ChatContact> contactList = new ArrayList<>();


    ChatUser userOne = new ChatUser();

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    ChatEngine engine;
    ValueEventListener valueEventListener;
    private SharedPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
//        Bundle bundle = getIntent().getExtras();
//        if (bundle != null) {
//            userOne = (ChatUser) getIntent().getSerializableExtra("Current User");
//        }
        userPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String userString = userPreferences.getString("loggedInUser", "");
        userOne = gson.fromJson(userString, ChatUser.class);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.clear();
                chatThreadDetails.clear();
                secondUsers.clear();
                friendlyMessageList.clear();
                contactList.clear();
                DataSnapshot userSnap = dataSnapshot.child("login");
                Iterable<DataSnapshot> userDetails = userSnap.getChildren();
                for (DataSnapshot s : userDetails) {
                    ChatUser user = s.getValue(ChatUser.class);
                    users.add(user);
                }

                DataSnapshot threadSnap = dataSnapshot.child("thread");
                Iterable<DataSnapshot> threadDetails = threadSnap.getChildren();

                for (DataSnapshot snap : threadDetails) {
                    ChatThread chatThread = snap.getValue(ChatThread.class);
                    //userOne = (ChatUser) getIntent().getSerializableExtra("Current User");
                    if (userOne.getUserName().equals(chatThread.getMessengerOne()) || userOne.getUserName().equals(chatThread.getMessengerTwo())) {
                        chatThreadDetails.add(chatThread);
                    }
                }
                for (ChatThread t : chatThreadDetails) {
                    String secondUsername = "";
                    if (t.getMessengerOne().equals(userOne.getUserName())) {
                        secondUsername = t.getMessengerTwo();
                    } else {
                        secondUsername = t.getMessengerOne();
                    }
                    for (ChatUser u : users) {
                        if (u.getUserName().equals(secondUsername))
                            secondUsers.add(u);
                    }
                }
                FriendlyMessage friendlyMessage = null;
                for (ChatThread chatThread : chatThreadDetails) {
                    DataSnapshot messagesSnap = dataSnapshot.child(chatThread.getThreadId());
                    Iterable<DataSnapshot> lastmsgDetails = messagesSnap.getChildren();
                    for (DataSnapshot snap : lastmsgDetails) {
                        friendlyMessage = snap.getValue(FriendlyMessage.class);
                    }
                    friendlyMessageList.add(friendlyMessage);
                    Log.d("Friendly Message", "Friendly Message " + friendlyMessage.getThreadId() + " : " + friendlyMessage.getText() + friendlyMessage.getName());
                }
                for (int i = 0; i < friendlyMessageList.size(); i++) {
                    ChatContact contact = new ChatContact();
                    contact.setFirstName(secondUsers.get(i).getFirstName());
                    contact.setLastName(secondUsers.get(i).getLastName());
                    contact.setThreadId(friendlyMessageList.get(i).getThreadId());
                    contact.setLastMessage(friendlyMessageList.get(i).getText());
                    contact.setUserName(secondUsers.get(i).getUserName());
                    contactList.add(contact);
                }
                engine = new ChatEngine(userOne, contactList);
                recyclerView = findViewById(R.id.recyclerViewChatList);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                adapter = new ChatAdapter(engine, new ChatItemClickListener() {
                    @Override
                    public void onChatClickListener(ChatContact contact) {
                        Intent contactIntent = new Intent(getApplicationContext(), MainActivity.class);
                        contactIntent.putExtra("Current User", userOne);
                        contactIntent.putExtra("Other UserName", contact.getUserName());
                        contactIntent.putExtra("Other User FirstName", contact.getFirstName());
                        contactIntent.putExtra("Other User LastName", contact.getLastName());
                        contactIntent.putExtra("Thread", contact.getThreadId());
                        startActivity(contactIntent);
                    }

                    @Override
                    public void onNewChatClickListener(ChatUser user) {
                    }
                });
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        update();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newChatIntent = new Intent(getApplicationContext(), NewChat.class);
                newChatIntent.putExtra("AllUsers", users);
                newChatIntent.putExtra("OtherUsers", secondUsers);
                newChatIntent.putExtra("Current User", userOne);
                startActivity(newChatIntent);
            }
        });
    }

    private void update() {
        mFirebaseDatabaseReference.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
    //    Toast.makeText(this, "OnResume", Toast.LENGTH_LONG).show();
        update();
    }

    @Override
    protected void onPause() {
        super.onPause();
      //  Toast.makeText(this, "OnPause", Toast.LENGTH_LONG).show();
        mFirebaseDatabaseReference.removeEventListener(valueEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseDatabaseReference.removeEventListener(valueEventListener);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // l̥Toast.makeText(this, "OnRestart", Toast.LENGTH_LONG).show();
        update();
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogTheme);
        // Setting Alert Dialog Title
        alertDialogBuilder.setTitle("Confirm Exit");
        // Icon Of Alert Dialog
        //alertDialogBuilder.setIcon(R.drawable.);
        // Setting Alert Dialog Message
        alertDialogBuilder.setMessage("Are you sure you want to exit?");
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                ChatListActivity.super.onBackPressed();
                finishAndRemoveTask();
                //System.exit(0);
            }
        });

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ChatListActivity.super.onBackPressed();
                Toast.makeText(ChatListActivity.this,"You clicked No",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ChatListActivity.this,ChatListActivity.class));
            }
        });
        alertDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),"You clicked Cancel",Toast.LENGTH_SHORT).show();
            }
        });

        alertDialogBuilder.show();

//        AlertDialog alertDialog = alertDialogBuilder.create();
//        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.meeting_invite).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                SharedPreferences.Editor editor = userPreferences.edit();
                editor.remove("loggedInUser");
                editor.apply();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

//TODO overflow options in inflator