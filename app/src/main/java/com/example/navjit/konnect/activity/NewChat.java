package com.example.navjit.konnect.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.navjit.konnect.R;
import com.example.navjit.konnect.model.ChatContact;
import com.example.navjit.konnect.model.ChatItemClickListener;
import com.example.navjit.konnect.model.ChatThread;
import com.example.navjit.konnect.model.ChatUser;
import com.example.navjit.konnect.model.FriendlyMessage;
import com.example.navjit.konnect.model.NewChatAdapter;
import com.example.navjit.konnect.model.NewChatEngine;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NewChat extends AppCompatActivity {

    List<ChatUser> users = new ArrayList<>();
    List<ChatUser> otherUsers = new ArrayList<>();
    List<ChatUser> userToDisplay = new ArrayList<>();
    List<ChatUser> usersNotToDisplay = new ArrayList<>();
    ChatUser currentUser;
    NewChatEngine engine;
    RecyclerView recyclerView;
    long threadCounter;
    RecyclerView.Adapter adapter;
    EditText editTextSearchBar;
    private DatabaseReference mFirebaseDatabaseReference;

    private double setThreadCounter(long counter) {
        double count = counter + 1;
        return count;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);
        recyclerView = findViewById(R.id.recyclerView);
        editTextSearchBar = findViewById(R.id.editTextSearchBar);
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            users = (ArrayList<ChatUser>) getIntent().getSerializableExtra("AllUsers");
            otherUsers = (ArrayList<ChatUser>) getIntent().getSerializableExtra("OtherUsers");
            currentUser = (ChatUser) getIntent().getSerializableExtra("Current User");
        }
        usersNotToDisplay.addAll(otherUsers);
        usersNotToDisplay.add(currentUser);

        users.removeAll(usersNotToDisplay);

        engine = new NewChatEngine(currentUser, users);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new NewChatAdapter(engine, new ChatItemClickListener() {


            @Override
            public void onChatClickListener(ChatContact contact) {

            }

            @Override
            public void onNewChatClickListener(ChatUser user) {
                mFirebaseDatabaseReference.child("threadCounter").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        threadCounter = dataSnapshot.getValue(Long.class);
                        threadCounter+=1;
                        mFirebaseDatabaseReference.child("threadCounter").setValue(threadCounter);

//                        Intent contactIntent = new Intent(getApplicationContext(), MainActivity.class);
//                        contactIntent.putExtra("Current User", currentUser);
//                        contactIntent.putExtra("Other User", user);
//                        contactIntent.putExtra("New Chat",true);
//                        String threadId = "messages"+threadCounter;
//                        contactIntent.putExtra("Thread", threadId);
//                        startActivity(contactIntent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        recyclerView.setAdapter(adapter);
        editTextSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before,
                                      int count) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext());
                alertDialog.setTitle("Text Changed..");
                alertDialog.setMessage("Character : " + charSequence.toString());
                alertDialog.setCancelable(false);

                AlertDialog alert = alertDialog.create();
                alert.show();

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }
}
