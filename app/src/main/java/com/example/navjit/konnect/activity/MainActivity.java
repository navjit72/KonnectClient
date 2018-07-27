/****************************************************************************************
 *     Author : Navjit Kaur
 *     Modified by : Harshdeep Singh
 *
 *     This activity displays all the users with which the current user has a chat with.
 *******************************************************************************************/

package com.example.navjit.konnect.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.navjit.konnect.R;
import com.example.navjit.konnect.model.ChatUser;
import com.example.navjit.konnect.model.FriendlyMessage;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {
    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder> mFirebaseAdapter;

    private static String THREAD_ID;
    private ChatUser currentUser;

    private static final String TAG = "MainActivity";
    private static final int REQUEST_INVITE = 1;
    private static final int REQUEST_IMAGE = 2;
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 10;
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private String mUsername;
    private String mPhotoUrl;

    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private EditText mMessageEditText;
    private ImageView mAddMessageImageView;
    String otherUserName;
    String otherUserFirstName;
    String otherUserLastName;
    private SharedPreferences userPreferences;

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        ImageView messageImageView;
        TextView messengerTextView;
        CircleImageView sentMessengerImageView;
        CircleImageView receivedMessengerImageView;

        public MessageViewHolder(View v) {
            super(v);
            receivedMessengerImageView = itemView.findViewById(R.id.receivedMessengerImageView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            sentMessengerImageView = itemView.findViewById(R.id.sentMessengerImageView);
            messageImageView = itemView.findViewById(R.id.messageImageView);
            messengerTextView = itemView.findViewById(R.id.messengerTextView);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle bundle = getIntent().getExtras();
        userPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
        if (bundle != null) {
            THREAD_ID = bundle.getString("Thread");
            currentUser = (ChatUser) getIntent().getSerializableExtra("Current User");
            mUsername = currentUser.getFirstName() + " " + currentUser.getLastName();
            otherUserName = bundle.getString("Other UserName");
            otherUserFirstName = bundle.getString("Other User FirstName");
            otherUserLastName = bundle.getString("Other User LastName");
        }
        this.setTitle(otherUserFirstName + " " + otherUserLastName);
        // mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        // New child entries
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabaseReference.child(THREAD_ID);
        SnapshotParser<FriendlyMessage> parser = new SnapshotParser<FriendlyMessage>() {
            @Override
            public FriendlyMessage parseSnapshot(DataSnapshot dataSnapshot) {
                FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                if (friendlyMessage != null) {
                    friendlyMessage.setId(dataSnapshot.getKey());
                }
                return friendlyMessage;
            }
        };

        //getting all user details from firebase in loginDetails.
        mFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot loginSnap = dataSnapshot.child("login");
                Iterable<DataSnapshot> loginChildren = loginSnap.getChildren();
                ArrayList<ChatUser> loginDetails = new ArrayList<>();

                for (DataSnapshot snap : loginChildren) {
                    ChatUser chatUser = snap.getValue(ChatUser.class);

                    loginDetails.add(chatUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //creating layout for messages including the user photos and populating the layout inflator
        DatabaseReference messagesRef = mFirebaseDatabaseReference.child(THREAD_ID);
        FirebaseRecyclerOptions<FriendlyMessage> options =
                new FirebaseRecyclerOptions.Builder<FriendlyMessage>()
                        .setQuery(messagesRef, parser)
                        .build();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>(options) {
            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new MessageViewHolder(inflater.inflate(R.layout.item_message, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(final MessageViewHolder viewHolder,
                                            int position,
                                            FriendlyMessage friendlyMessage) {
                if (friendlyMessage.getText() != null) {

                    if (isCurrentUserSender(friendlyMessage)) {
                        configureLayoutForSender(viewHolder, friendlyMessage);
                    } else {
                        configureLayoutForReceiver(viewHolder, friendlyMessage);
                    }

                } else {
                    String imageUrl = friendlyMessage.getImageUrl();
                    if (imageUrl.startsWith("gs://")) {
                        StorageReference storageReference = FirebaseStorage.getInstance()
                                .getReferenceFromUrl(imageUrl);
                        storageReference.getDownloadUrl().addOnCompleteListener(
                                new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            String downloadUrl = task.getResult().toString();
                                            Glide.with(viewHolder.messageImageView.getContext())
                                                    .load(downloadUrl)
                                                    .into(viewHolder.messageImageView);
                                        } else {
                                            Log.w(TAG, "Getting download url was not successful.",
                                                    task.getException());
                                        }
                                    }
                                });
                    } else {
                        Glide.with(viewHolder.messageImageView.getContext())
                                .load(friendlyMessage.getImageUrl())
                                .into(viewHolder.messageImageView);
                    }

                    viewHolder.messageImageView.setVisibility(ImageView.VISIBLE);
                    viewHolder.messageTextView.setVisibility(TextView.GONE);
                }

                if (friendlyMessage.getPhotoUrl() == null) {
                    if (isCurrentUserSender(friendlyMessage)) {
                        viewHolder.sentMessengerImageView.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,
                                R.drawable.ic_account_circle_black_36dp));
                    } else {
                        viewHolder.receivedMessengerImageView.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,
                                R.drawable.ic_account_circle_black_36dp));
                    }
                } else {
                    Glide.with(MainActivity.this)
                            .load(friendlyMessage.getPhotoUrl())
                            .into(viewHolder.sentMessengerImageView);
                }
            }
        };

        //scrolling to last to view the last message
        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        //setting adapter and layout manager to recycler view
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);

        //to enable disable send button on text change
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        //disabling the ability to send message on broadcast if the user is student
        mAddMessageImageView = (ImageView) findViewById(R.id.addMessageImageView);
        mSendButton = (Button) findViewById(R.id.sendButton);
        if (THREAD_ID.equals("broadcast") && currentUser.getUserType().equals("student")) {
            mAddMessageImageView.setVisibility(View.GONE);
            mSendButton.setVisibility(View.GONE);
            mMessageEditText.setVisibility(View.GONE);
        }

        //sending the message on clicking send button
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendlyMessage friendlyMessage = new
                        FriendlyMessage(mMessageEditText.getText().toString(),
                        mUsername,
                        mPhotoUrl,
                        null /* no image */, THREAD_ID);
                mFirebaseDatabaseReference.child(THREAD_ID)
                        .push().setValue(friendlyMessage);
                mMessageEditText.setText("");
            }
        });

        //opening local storage to send an attachment
        mAddMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Select image for image message on click.
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });
    }

    //finding if the message sent by current user.
    private boolean isCurrentUserSender(FriendlyMessage friendlyMessage) {
        return friendlyMessage.getName().equals(mUsername);
    }

    //creating layout for receiver
    private void configureLayoutForReceiver(MessageViewHolder viewHolder, FriendlyMessage friendlyMessage) {
        viewHolder.receivedMessengerImageView.setVisibility(View.VISIBLE);
        viewHolder.sentMessengerImageView.setVisibility(GONE);
        viewHolder.messageImageView.setVisibility(GONE);
        viewHolder.messageTextView.setText(friendlyMessage.getText());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.START;
        viewHolder.messengerTextView.setText(friendlyMessage.getName());
        viewHolder.messageTextView.setLayoutParams(params);
        viewHolder.messengerTextView.setLayoutParams(params);
    }

    //creating layout for sender
    private void configureLayoutForSender(MessageViewHolder viewHolder, FriendlyMessage friendlyMessage) {
        viewHolder.sentMessengerImageView.setVisibility(View.VISIBLE);
        viewHolder.receivedMessengerImageView.setVisibility(GONE);
        viewHolder.messageImageView.setVisibility(GONE);
        viewHolder.messageTextView.setText(friendlyMessage.getText());
        viewHolder.messengerTextView.setText(friendlyMessage.getName());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END;
        params.setMarginEnd((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10, getResources()
                        .getDisplayMetrics()));
        viewHolder.messageTextView.setLayoutParams(params);
        viewHolder.messengerTextView.setLayoutParams(params);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //populating menu on the basis of usertype
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //if user is student the meeting invite button will be disabled.
        if (currentUser.getUserType().equals("student")) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_menu, menu);
            menu.findItem(R.id.meeting_invite).setVisible(false);
        } else {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_menu, menu);
        }
        return true;
    }

    //providing actions based on item selected from overflow menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //on signing out user session is closed.
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                SharedPreferences.Editor editor = userPreferences.edit();
                editor.remove("loggedInUser");
                editor.apply();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;

            //moving on to meeting invite activity on selecting meeting invite iem from menu
            case R.id.meeting_invite:
                Bundle bundle = new Bundle();
                bundle.putString("User", mUsername);
                CreateMeetingActivity.launch(this, bundle);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // An unresolvable error has occurred and Google APIs will not be available.
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    //on creating a new chat if user does not send any message and go back then the newly created thread is deleted.
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mFirebaseDatabaseReference.child(THREAD_ID).addListenerForSingleValueEvent(new ValueEventListener() {
            Long cnt = 0L;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    mFirebaseDatabaseReference.child("threadCounter").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //getting current threadcounter value from database
                            cnt = Long.parseLong(dataSnapshot.getValue().toString());
                            //removing that particular thread with the current threadcounter value.
                            mFirebaseDatabaseReference.child("thread").child(cnt.toString()).removeValue();
                            cnt -= 1;
                            //setting value of threadcounter with decremented value
                            mFirebaseDatabaseReference.child("threadCounter").setValue(cnt);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //moving back to chat list
        Intent chatListIntent = new Intent(this, ChatListActivity.class);
        chatListIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        chatListIntent.putExtra("Current User", currentUser);
        startActivity(chatListIntent);
    }

    //sending image as message
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();

                    FriendlyMessage tempMessage = new FriendlyMessage(null, mUsername, mPhotoUrl, uri.toString(),THREAD_ID);
                    mFirebaseDatabaseReference.child(THREAD_ID).push()
                            .setValue(tempMessage, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError,
                                                       DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        String key = databaseReference.getKey();
                                        StorageReference storageReference =
                                                FirebaseStorage.getInstance()
                                                        .getReference(mFirebaseUser.getUid())
                                                        .child(key)
                                                        .child(uri.getLastPathSegment());

                                        putImageInStorage(storageReference, uri, key);
                                    } else {
                                        Log.w(TAG, "Unable to write message to database.",
                                                databaseError.toException());
                                    }
                                }
                            });
                }
            }
        }
    }

    //storing image path in firebase
    private void putImageInStorage(StorageReference storageReference, Uri uri, final String key) {
        storageReference.putFile(uri).addOnCompleteListener(MainActivity.this,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            FriendlyMessage friendlyMessage =
                                    new FriendlyMessage(null, mUsername, mPhotoUrl, task.getResult().getMetadata().getPath(),THREAD_ID);
                            mFirebaseDatabaseReference.child(THREAD_ID).child(key).setValue(friendlyMessage);
                        } else {
                            Log.w(TAG, "Image upload task was not successful.", task.getException());
                        }
                    }
                });
        }
    }
