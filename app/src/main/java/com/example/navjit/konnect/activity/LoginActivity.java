package com.example.navjit.konnect.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.navjit.konnect.model.ChatUser;
import com.example.navjit.konnect.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private DatabaseReference mFirebaseDatabaseReference;
    private ArrayList<String> usernameDetails = new ArrayList<>();
    private ArrayList<ChatUser> users =new ArrayList<>();
    private AutoCompleteTextView editTextUsername;
    private EditText editTextPassword;
    private Button buttonSignIn;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignIn = findViewById(R.id.buttonSignIn);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        fetchUserData();
        addUserDataToAutoCompleteView();

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trySignIn();
            }
        });
    }

    private void addUserDataToAutoCompleteView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,usernameDetails);
        editTextUsername.setAdapter(adapter);
    }

    private void fetchUserData() {
        mFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot loginSnap = dataSnapshot.child("users");
                Iterable<DataSnapshot> loginChildren = loginSnap.getChildren();

                for(DataSnapshot snap : loginChildren){
                    ChatUser login = snap.getValue(ChatUser.class);
                    //Log.d("ChatUser" , "ChatUser username : " + login.getUserName());
                    usernameDetails.add(login.getUserName());
                    users.add(login);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("onCancelled", databaseError.getDetails());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        editTextUsername.setText("");
        editTextPassword.setText("");
    }

    private void trySignIn() {
        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();

        if (username.equals("") || password.equals("")) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
        } else {
            if (usernameDetails.contains(username)) {
                signIn(getUser(username), password);
            } else {
                createToast("Sign In Failed");
            }
        }
    }

    private ChatUser getUser(String username) {
        for(ChatUser user : users)
        {
            if(user.getUserName().equals(username)) {
                return user;
            }
        }
        return null;
    }

    private void signIn(ChatUser user, String password) {
        mAuth.signInWithEmailAndPassword(user.getEmail(), password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("Login", "trySignIn:onComplete:" + task.isSuccessful());
                        if (task.isSuccessful()) {
                            createToast("Logged in successfully");
                            goToChatList(user);
                        } else {
                            createToast("Sign In Failed");
                            editTextUsername.setText("");
                            editTextPassword.setText("");
                        }
                    }
                });
    }

    private void goToChatList(ChatUser user) {
        Intent detailsIntent =  new Intent(getApplicationContext(),ChatListActivity.class);
        detailsIntent.putExtra("Username", editTextUsername.getText().toString());
        detailsIntent.putExtra("UserType", user.getUserType());
        detailsIntent.putExtra("FirstName", user.getFirstName());
        detailsIntent.putExtra("LastName", user.getLastName());
        startActivity(detailsIntent);
    }

    private void createToast(String message) {
        Toast.makeText(LoginActivity.this, message,
                Toast.LENGTH_SHORT).show();
    }
}
