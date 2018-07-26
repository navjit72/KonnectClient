package com.example.navjit.konnect.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.HttpHeaders;
import cz.msebera.android.httpclient.entity.StringEntity;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {
    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>
            mFirebaseAdapter;
    private static String THREAD_ID;
    private ChatUser currentUser;

    private static final String TAG = "MainActivity";
    private static final int REQUEST_INVITE = 1;
    private static final int REQUEST_IMAGE = 2;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 10;
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    private GoogleApiClient mGoogleApiClient;
    //private static final String MESSAGE_URL = "http://friendlychat.firebase.google.com/message/";

    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private ImageView mAddMessageImageView;
    String otherUserName;
    String otherUserFirstName;
    String otherUserLastName;
    String otherUserToken;
    private SharedPreferences userPreferences;
    ValueEventListener valueEventListener;

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
            THREAD_ID= bundle.getString("Thread");
            currentUser = (ChatUser) getIntent().getSerializableExtra("Current User");
            mUsername = currentUser.getFirstName() + " " + currentUser.getLastName();
            otherUserName = bundle.getString("Other UserName");
            otherUserFirstName = bundle.getString("Other User FirstName");
            otherUserLastName = bundle.getString("Other User LastName");
            otherUserToken = bundle.getString("Other User Token");
        }
        this.setTitle(otherUserFirstName + " " + otherUserLastName);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Set default username is anonymous.
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mProgressBar = findViewById(R.id.progressBar);

        //Modify your MainActivity's onCreate method by replacing mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        // with the code defined below. This code initially adds all existing messages and then listens for new child
        // entries under the messages path in your Firebase Realtime Database. It adds a new element to the UI for each
        // message:
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

        mFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot loginSnap = dataSnapshot.child("login");
                Iterable<DataSnapshot> loginChildren = loginSnap.getChildren();
                ArrayList<ChatUser> loginDetails = new ArrayList<>();

                for(DataSnapshot snap : loginChildren){
                    ChatUser chatUser = snap.getValue(ChatUser.class);

                    loginDetails.add(chatUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

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
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                if (friendlyMessage.getText() != null) {

                    if (isCurrentUserSender(friendlyMessage)) {
                        configureLayoutForSender(viewHolder, friendlyMessage);
                    } else {
                        configureLayoutForReceiver(viewHolder, friendlyMessage);
                    }

                }

                else if (friendlyMessage.getImageUrl() != null) {
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

        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);

        mMessageRecyclerView.setAdapter(mFirebaseAdapter);


        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);

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
        mAddMessageImageView = (ImageView) findViewById(R.id.addMessageImageView);
        mSendButton = (Button) findViewById(R.id.sendButton);
        if(THREAD_ID.equals("broadcast") && currentUser.getUserType().equals("student")){
            mAddMessageImageView.setVisibility(View.GONE);
            mSendButton.setVisibility(View.GONE);
            mMessageEditText.setVisibility(View.GONE);
        }
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

                String url = "https://fcm.googleapis.com/fcm/send";
                AsyncHttpClient client = new AsyncHttpClient();
                client.addHeader(HttpHeaders.AUTHORIZATION, "key=AIzaSyAiVsW00ommP7msOLZyiIrRvwMjfAeMs0A");
                client.addHeader(HttpHeaders.CONTENT_TYPE, RequestParams.APPLICATION_JSON);
                try {
                    JSONArray registrationTokens = new JSONArray();

                    registrationTokens.put(otherUserToken);

                    JSONObject message = new JSONObject();
                    message.put("registration_ids", registrationTokens);

                    JSONObject notification = new JSONObject();
                    notification.put("title", friendlyMessage.getName());
                    notification.put("body", friendlyMessage.getText());

                    message.put("notification", notification);

                    StringEntity entity = new StringEntity(message.toString());

                    client.post(getApplicationContext(), url, entity, RequestParams.APPLICATION_JSON, new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {

                            Log.i(TAG, responseString);
                        }

                        @Override
                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {

                            Log.i(TAG, responseString);
                        }
                    });

                } catch (Exception e) {

                }


                mMessageEditText.setText("");
            }
        });
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

    private boolean isCurrentUserSender(FriendlyMessage friendlyMessage) {
        return friendlyMessage.getName().equals(mUsername);
    }

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
        // Check if user is signed in.
        // TODO: Add code to check if user is signed in.
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        if(currentUser.getUserType().equals("student")) {
            menu.findItem(R.id.meeting_invite).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                SharedPreferences.Editor editor = userPreferences.edit();
                editor.remove("loggedInUser");
                editor.apply();
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            case R.id.meeting_invite:
                Bundle bundle = new Bundle();
                bundle.putString("User", mUsername);
                CreateMeetingActivity.launch(this, bundle);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("thread ID", "thread id : " + THREAD_ID);
        Log.d("Sender","sender username : " + currentUser.getUserName());
        Log.d("Receiver", "receiver username : "  + otherUserName);

        if(mFirebaseDatabaseReference.child(THREAD_ID)==null){
            Log.d("Thread id","null");
        }
        else {
            Log.d("Thread id", "not null");
        }

        mFirebaseDatabaseReference.child(THREAD_ID).addListenerForSingleValueEvent(new ValueEventListener() {
            Long cnt=0L;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("MainActivity","msg : "+ dataSnapshot.getValue());
                if(dataSnapshot.getValue() == null){
                    mFirebaseDatabaseReference.child("threadCounter").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {;
                            cnt = Long.parseLong(dataSnapshot.getValue().toString());
                            Log.d("thread counter","cnt : " + cnt);
                            mFirebaseDatabaseReference.child("thread").child(cnt.toString()).removeValue();
                            cnt -= 1;
                            mFirebaseDatabaseReference.child("threadCounter").setValue(cnt);
                        }

                        @Override
                        public void onCancelled( DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Intent chatListIntent = new Intent(this,ChatListActivity.class);
        chatListIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        chatListIntent.putExtra("Current User",currentUser);
        startActivity(chatListIntent);
    }
}
