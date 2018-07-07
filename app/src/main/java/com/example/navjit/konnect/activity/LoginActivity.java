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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private DatabaseReference mFirebaseDatabaseReference;
    ArrayList<String> usernameDetails = new ArrayList<>();
    ArrayList<ChatUser> loginDetails=new ArrayList<>();
    AutoCompleteTextView editTextUsername;
    EditText editTextPassword;
    Button buttonSignIn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignIn = findViewById(R.id.buttonSignIn);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        signIn();

        mFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot loginSnap = dataSnapshot.child("login");
                Iterable<DataSnapshot> loginChildren = loginSnap.getChildren();

                for(DataSnapshot snap : loginChildren){
                    ChatUser login = snap.getValue(ChatUser.class);
                    //Log.d("ChatUser" , "ChatUser username : " + login.getUserName());
                    usernameDetails.add(login.getUserName());
                    loginDetails.add(login);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,usernameDetails);
        editTextUsername.setAdapter(adapter);

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(ChatUser l : loginDetails)
                {
                    if(l.getUserName().equals(editTextUsername.getText().toString()))
                    {
                        if(l.getPassword().equals(editTextPassword.getText().toString()))
                        {
                            //Intent detailsIntent =  new Intent(getApplicationContext(),MainActivity.class);
                            Intent detailsIntent =  new Intent(getApplicationContext(),ChatListActivity.class);
//                            detailsIntent.putExtra("Username", editTextUsername.getText().toString());
//                            detailsIntent.putExtra("UserType", l.getUserType());
//                            detailsIntent.putExtra("FirstName", l.getFirstName());
//                            detailsIntent.putExtra("LastName", l.getLastName());
                            detailsIntent.putExtra("Current User",l);
                            startActivity(detailsIntent);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"Invalid Password. Please try again." ,Toast.LENGTH_LONG).show();
                            editTextUsername.setText("");
                            editTextPassword.setText("");
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        editTextUsername.setText("");
        editTextPassword.setText("");
    }

    private void signIn() {

        String email = "info@konnect.com";
        String password = "konnect";

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                       // Log.d("Login", "signIn:onComplete:" + task.isSuccessful());
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Connected to Server!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Sign In Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
