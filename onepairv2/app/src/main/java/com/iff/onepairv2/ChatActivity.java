package com.iff.onepairv2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser_target_uid;
    private String mChatUser_target_name;
    private String mChatUser_target_image;

    private String mChatUser_own_uid;
    private String mChatUser_own_name;
    private String mChatUser_own_image;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;

    //xml elements
    private ImageView mChat_target_image;
    private EditText mMsg_field;
    private ImageButton mSend_btn;
    private RecyclerView mMessagesList;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private RequestQueue mRequestQue;
    private String URL = "https://fcm.googleapis.com/fcm/send";
    private String topic;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mRequestQue = Volley.newRequestQueue(this);

        //Chat Target Details
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mChatUser_target_uid = getIntent().getStringExtra("user_id");
        mChatUser_target_name = getIntent().getStringExtra("user_name");
        mChatUser_target_image = getIntent().getStringExtra("user_image");

        //Set Picture in Toolbar
        mChat_target_image = findViewById(R.id.chat_pic);
        Picasso.get().load(mChatUser_target_image).into(mChat_target_image);

        //Set Title in Toolbar
        mToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("          " + mChatUser_target_name);

        //Current User Details
        mAuth = FirebaseAuth.getInstance();
        mChatUser_own_uid = mAuth.getCurrentUser().getUid();
       /* mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mChatUser_own_uid);
        mUserDatabase.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mChatUser_own_name = dataSnapshot.child("name").getValue().toString();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        //xml elements
        mMsg_field = (EditText) findViewById(R.id.msg_field);
        mSend_btn = (ImageButton) findViewById(R.id.chat_send_btn);
        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        mLinearLayout = new LinearLayoutManager(this);

        mAdapter = new MessageAdapter(messagesList);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);
        loadMesseges();


        //Create Chat in Database
        mRootRef.child("Chat").child(mChatUser_own_uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUser_target_uid)) {

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("chat", true);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mChatUser_own_uid + "/" + mChatUser_target_uid, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser_target_uid + "/" + mChatUser_own_uid, chatAddMap);

                    mRootRef.updateChildren(chatUserMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Create Conversation
        mSend_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (item.getItemId() == R.id.unmatch) {
            mRootRef.child("Chat").child(mChatUser_own_uid).child(mChatUser_target_uid).child("chat").setValue(false);
            mRootRef.child("Chat").child(mChatUser_target_uid).child(mChatUser_own_uid).child("chat").setValue(false);
            Intent startIntent = new Intent(ChatActivity.this, MatchedPersons.class);
            startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(startIntent);
            finish();
        } else if (item.getItemId() == R.id.close_chat) {
            mRootRef.child("Chat").child(mChatUser_own_uid).child(mChatUser_target_uid).child("chat").setValue(false);
            Intent startIntent = new Intent(ChatActivity.this, MatchedPersons.class);
            startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(startIntent);
            finish();
        }
        //return super.onOptionsItemSelected(item);
        return false;
    }

    private void loadMesseges() {
        mRootRef.child("Messages").child(mChatUser_own_uid).child(mChatUser_target_uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(messagesList.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {

        String message = mMsg_field.getText().toString();

        if (!TextUtils.isEmpty(message)) {

            String current_user_ref = "Messages/" + mChatUser_own_uid + "/" + mChatUser_target_uid;
            String target_user_ref = "Messages/" + mChatUser_target_uid + "/" + mChatUser_own_uid;

            DatabaseReference user_message_push = mRootRef.child("Messages").child(mChatUser_own_uid).child(mChatUser_target_uid).push();
            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mChatUser_own_uid);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id + "/", messageMap);
            messageUserMap.put(target_user_ref + "/" + push_id + "/", messageMap);

            mMsg_field.setText("");

            mRootRef.updateChildren(messageUserMap);

            sendNotification(message);

        }
    }

    private void sendNotification(String message){

        //for sending of background notifications
        //our json object will look like this
        JSONObject mainObj = new JSONObject();
        topic = mChatUser_own_uid + mChatUser_target_uid;
        System.out.println("NAME NAME" + mChatUser_own_name);
        try {
            mainObj.put("to", "/topics/" + topic);
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", "New Message");
            notificationObj.put("body", message);
            mainObj.put("notification", notificationObj);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, mainObj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    //codes here will run when notification is sent
                    System.out.println("MESSAGE SENT");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //codes here will run on error
                    System.out.println("MESSAGE FAILED");
                }
            }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=AIzaSyAOPEEMta24s-K-XyunD5xpBGsNQ6FGcwc");
                    return header;
                }
            };
            mRequestQue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /*Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("user_id", mChatUser_own_uid);


        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "123");

        builder.setContentIntent(pendingIntent);*/

    }
}
