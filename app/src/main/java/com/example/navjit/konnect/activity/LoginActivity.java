/********************************************************************
 *     Author : Harshdeep Singh
 *
 *     This activity displays is the landing page to login into app.
 ********************************************************************/

package com.example.navjit.konnect.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.navjit.konnect.R;
import com.example.navjit.konnect.model.ChatUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private DatabaseReference mFirebaseDatabaseReference;
    ArrayList<String> usernameDetails = new ArrayList<>();
    ArrayList<ChatUser> loginDetails = new ArrayList<>();
    AutoCompleteTextView editTextUsername;
    EditText editTextPassword;
    Button buttonSignIn;
    private FirebaseAuth mAuth;
    private SharedPreferences userPreferences;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        userPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        signIn();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, usernameDetails);
        editTextUsername.setAdapter(adapter);

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authoriseUser();
            }
        });

        if (userPreferences.contains("loggedInUser")) {
            Gson gson = new Gson();
            String userString = userPreferences.getString("loggedInUser", "");
            ChatUser loggedInUser = gson.fromJson(userString, ChatUser.class);
            goToChatDetails(loggedInUser);
        }
    }

    private void authoriseUser() {
        for (ChatUser l : loginDetails) {
            if (l.getUserName().equals(editTextUsername.getText().toString()) && !l.getUserType().equals("userType")) {
                if (l.getPassword().equals(editTextPassword.getText().toString())) {
                    l.setDeviceId(deviceId);
                    l.setDeviceToken(FirebaseInstanceId.getInstance().getToken());
                    updateDeviceDetails(l);
                    Gson gson = new Gson();
                    String userString = gson.toJson(l);
                    SharedPreferences.Editor editor = userPreferences.edit();
                    editor.putString("loggedInUser", userString);
                    editor.apply();
                    goToChatDetails(l);
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid Password. Please try again.", Toast.LENGTH_LONG).show();
                    editTextUsername.setText("");
                    editTextPassword.setText("");
                }
            }
        }
    }

    private void updateDeviceDetails(ChatUser l) {
        mFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot loginSnap = dataSnapshot.child("login");
                Iterable<DataSnapshot> loginChildren = loginSnap.getChildren();

                for (DataSnapshot snap : loginChildren) {
                    ChatUser login = snap.getValue(ChatUser.class);
                    if (login.getUserName().equals(l.getUserName())) {
                        mFirebaseDatabaseReference.child("login/" + snap.getKey() + "/deviceId").setValue(l.getDeviceId());
                        mFirebaseDatabaseReference.child("login/" + snap.getKey() + "/deviceToken").setValue(l.getDeviceToken());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void goToChatDetails(ChatUser l) {
        Intent detailsIntent = new Intent(getApplicationContext(), ChatListActivity.class);
        detailsIntent.putExtra("Current User", l);
        startActivity(detailsIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        editTextUsername.setText("");
        editTextPassword.setText("");
    }

    private void signIn() {

        String email = "info@konnect.com";
        String password = "password";

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Connected to Server!",
                                    Toast.LENGTH_SHORT).show();
                            mFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    DataSnapshot loginSnap = dataSnapshot.child("login");
                                    Iterable<DataSnapshot> loginChildren = loginSnap.getChildren();

                                    for (DataSnapshot snap : loginChildren) {
                                        ChatUser login = snap.getValue(ChatUser.class);
                                        usernameDetails.add(login.getUserName());
                                        loginDetails.add(login);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {
                            Toast.makeText(LoginActivity.this, "Sign In Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
                LoginActivity.super.onBackPressed();
                finishAndRemoveTask();
                //System.exit(0);
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
                Toast.makeText(getApplicationContext(),"You clicked Cancel",Toast.LENGTH_SHORT).show();
            }
        });

        alertDialogBuilder.show();

    }

}
