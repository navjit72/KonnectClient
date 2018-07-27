/******************************************************************
 *     Author : Navjit Kaur
 *
 *     This activity is to create a new chat with available users.
 *******************************************************************/
package com.example.navjit.konnect.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import com.example.navjit.konnect.R;
import com.example.navjit.konnect.model.ChatContact;
import com.example.navjit.konnect.model.ChatItemClickListener;
import com.example.navjit.konnect.model.ChatThread;
import com.example.navjit.konnect.model.ChatUser;
import com.example.navjit.konnect.model.NewChatAdapter;
import com.example.navjit.konnect.model.NewChatEngine;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NewChat extends AppCompatActivity {

    List<ChatUser> users = new ArrayList<>();   //List to store all the users present in database.
    List<ChatUser> otherUsers = new ArrayList<>();  //List to store the users that the current user has chat with.
    List<ChatUser> usersNotToDisplay = new ArrayList<>(); //List to store all the users that current user has chat with and the current user itself
    ChatUser currentUser;
    NewChatEngine engine;
    RecyclerView recyclerView;
    long threadCounter;
    RecyclerView.Adapter adapter;
    EditText editTextSearchBar;
    private DatabaseReference mFirebaseDatabaseReference;

//    private double setThreadCounter(long counter) {
//        double count = counter + 1;
//        return count;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ChatUser instructor = null;
        int count=0;        //to check how many times the instructor is present.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);
        recyclerView = findViewById(R.id.recyclerView);
        editTextSearchBar = findViewById(R.id.editTextSearchBar);
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Bundle bundle = getIntent().getExtras();

        //fetching lists from bundle.
        if (bundle != null) {
            users = (ArrayList<ChatUser>) getIntent().getSerializableExtra("AllUsers");
            otherUsers = (ArrayList<ChatUser>) getIntent().getSerializableExtra("OtherUsers");
            currentUser = (ChatUser) getIntent().getSerializableExtra("Current User");
        }
        usersNotToDisplay.addAll(otherUsers);
        usersNotToDisplay.add(currentUser);
        for(ChatUser u :otherUsers){
            //to check if the current user has a chat with the instructor
            if(u.getUserType().equals("instructor")) {
                count++;
                instructor = u;
            }
        }

        //if the chat with the user is only broadcast then add the instructor in the list of users avaailable to chat.
        if(count==1){
            usersNotToDisplay.remove(instructor);
        }

        //removing the users who had chat with current user from the list of all users.
        users.removeAll(usersNotToDisplay);

        //filtering the list to fetch only the instructor and students.
        users = users.stream()
                .filter(line -> line.getUserType().toLowerCase().equals("student") ||
                        line.getUserType().toLowerCase().equals("instructor"))
                .collect(Collectors.toList());
        engine = new NewChatEngine(currentUser, users);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        ChatItemClickListener listener = new ChatItemClickListener() {


            @Override
            public void onChatClickListener(ChatContact contact) {

            }

            //if current user click on any user from the list flow will transfer to the main activity.
            @Override
            public void onNewChatClickListener(ChatUser user) {
                mFirebaseDatabaseReference.child("threadCounter").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //getting the curent threadcounter value, incrementing it and setting the new value.
                        threadCounter = dataSnapshot.getValue(Long.class);
                        threadCounter += 1;
                        mFirebaseDatabaseReference.child("threadCounter").setValue(threadCounter);

                        //creating a thread for the newly created chat in database.
                        ChatThread chatThread = new ChatThread("messages" + (100 + threadCounter), currentUser.getUserName(), user.getUserName());
                        mFirebaseDatabaseReference.child("thread").child("" + threadCounter).setValue(chatThread);

                        Intent contactIntent = new Intent(getApplicationContext(), MainActivity.class);
                        contactIntent.putExtra("Current User", currentUser);
                        contactIntent.putExtra("Other UserName", user.getUserName());
                        contactIntent.putExtra("Other User FirstName", user.getFirstName());
                        contactIntent.putExtra("Other User LastName", user.getLastName());
                        contactIntent.putExtra("New Chat", true);
                        String threadId = "messages" + (100 + threadCounter);
                        contactIntent.putExtra("Thread", threadId);
                        startActivity(contactIntent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };

        //setting adapter to recycler view
        adapter = new NewChatAdapter(engine, listener);
        recyclerView.setAdapter(adapter);

        //updating recycler view based on the text entered by current user in the search bar.
        editTextSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before,
                                      int count) {
                String searchText = charSequence.toString();
                List<ChatUser> result = users.stream()
                        .filter(line -> line.getUserName().toLowerCase().contains(searchText.toLowerCase())
                                || line.getFirstName().toLowerCase().contains(searchText.toLowerCase())
                                || line.getLastName().toLowerCase().contains(searchText.toLowerCase()))
                        .collect(Collectors.toList());
                engine = new NewChatEngine(currentUser, result);
                adapter = new NewChatAdapter(engine, listener);
                recyclerView.setAdapter(adapter);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }
}