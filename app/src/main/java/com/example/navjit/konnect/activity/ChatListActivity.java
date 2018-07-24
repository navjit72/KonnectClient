/****************************************************************************************
 *     Author : Navjit Kaur
 *     Modified by : Harshdeep Singh
 *
 *     This activity displays all the users with which the current user has a chat with.
*******************************************************************************************/

package com.example.navjit.konnect.activity;

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
    ArrayList<ChatThread> chatThreadDetails = new ArrayList<>();    //List to store thread details of all chats that the current user has.
    ArrayList<ChatUser> users = new ArrayList<>();  //List to store all the users.
    ArrayList<ChatUser> secondUsers = new ArrayList<>();    //List to store the users that the current user has a chat with.
    ArrayList<FriendlyMessage> friendlyMessageList = new ArrayList<>();     //List to store details of all the messages.
    ArrayList<ChatContact> contactList = new ArrayList<>();     //List to store the users that has chat with current user with the last message sent.


    ChatUser userOne = new ChatUser();  //current user object.

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

        //storing the details of current user in shared preferences to avoid logging in everytime.
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
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

                //fetching the all the threads of current user and storing in the chat thread list.
                DataSnapshot threadSnap = dataSnapshot.child("thread");
                Iterable<DataSnapshot> threadDetails = threadSnap.getChildren();

                for (DataSnapshot snap : threadDetails) {
                    ChatThread chatThread = snap.getValue(ChatThread.class);
                    if(chatThread.getThreadId().equals("broadcast")){
                        chatThreadDetails.add(chatThread);
                    }
                    else if (userOne.getUserName().equals(chatThread.getMessengerOne()) || userOne.getUserName().equals(chatThread.getMessengerTwo())) {
                        chatThreadDetails.add(chatThread);
                    }
                }

                //populating the second users list with the users with which current user has chats.
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

                //populating the messages list with all the messages.
                FriendlyMessage friendlyMessage = null;
                for (ChatThread chatThread : chatThreadDetails) {
                    DataSnapshot messagesSnap = dataSnapshot.child(chatThread.getThreadId());
                    Iterable<DataSnapshot> lastmsgDetails = messagesSnap.getChildren();
                    for (DataSnapshot snap : lastmsgDetails) {
                        friendlyMessage = snap.getValue(FriendlyMessage.class);
                    }
                    friendlyMessageList.add(friendlyMessage);
                    Log.d("Friendly Message", "Friendly Message " + friendlyMessage.getThreadId() + " : " + friendlyMessage.getText() + " "+friendlyMessage.getName());
                }

                //populating the contact list with the second users details with the last message sent.
                for (int i = 0; i < friendlyMessageList.size(); i++) {
                    ChatContact contact = new ChatContact();
                    contact.setFirstName(secondUsers.get(i).getFirstName());
                    contact.setLastName(!userOne.getUserType().equals("instructor") && friendlyMessageList.get(i).getThreadId().equals("broadcast") ? secondUsers.get(i).getLastName()+" (broadcast) ":secondUsers.get(i).getLastName() );
                    contact.setThreadId(friendlyMessageList.get(i).getThreadId());
                    contact.setLastMessage(friendlyMessageList.get(i).getText());
                    contact.setUserName(secondUsers.get(i).getUserName());
                    contactList.add(contact);
                }

                //displaying the second users with the last message sent in the recycler view of chat list activity.
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

        //calling the value event listener again to update the recycler view.
        update();

        //navigating to new chat activity on click of floating button.
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
        //refreshing the recycler view by interacting with firebase on activity getting resumed.
        update();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        //refreshing recycler view by interacting firebase on restarting the activity.
        update();
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogTheme);
        alertDialogBuilder.setTitle("Confirm Exit");
        // Setting Alert Dialog Message
        alertDialogBuilder.setMessage("Are you sure you want to exit?");
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                ChatListActivity.super.onBackPressed();
                finishAndRemoveTask();
            }
        });

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialogBuilder.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //filling up the overflow menu
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