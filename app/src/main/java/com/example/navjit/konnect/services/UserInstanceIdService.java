package com.example.navjit.konnect.services;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.navjit.konnect.model.ChatUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.gson.Gson;

public class UserInstanceIdService extends FirebaseInstanceIdService {

    private SharedPreferences userPreferences;

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();

        if (token != null) {
            userPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
            sendTokenToServer(token);
        }
    }

    private void sendTokenToServer(String token) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference users = database.getReference("login");

        ChatUser loggedInUser = getLoggedInUser();

        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    ChatUser user = userSnapshot.getValue(ChatUser.class);

                    if (user.getDeviceId() != null && user.getDeviceId().equals(loggedInUser.getDeviceId())) {
                        user.setDeviceId(token);
                        users.child(userSnapshot.getKey()).child("deviceToken")
                                .setValue(token, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        if (databaseError != null) {
                                            Log.i("ERROR", databaseError.toString());
                                        } else {
                                            Log.i("SUCCESS", "Token refreshed");
                                        }
                                    }
                                });
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public ChatUser getLoggedInUser() {
        Gson gson = new Gson();
        String userString = userPreferences.getString("loggedInUser", "");
        return gson.fromJson(userString, ChatUser.class);
    }
}
