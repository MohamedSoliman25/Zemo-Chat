package com.example.zemochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.zemochat.Activity.UserInfo;
import com.example.zemochat.Adapter.MessageAdapter;
import com.example.zemochat.Constants.AllConstants;
import com.example.zemochat.Permissions.Permissions;
import com.example.zemochat.Utils.Util;
import com.example.zemochat.databinding.ActivityMessageBinding;
import com.example.zemochat.databinding.LeftItemLayoutBinding;
import com.example.zemochat.databinding.RightItemLayoutBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MessageActivity extends AppCompatActivity {
    private ActivityMessageBinding binding;
    private String hisID, hisImage, myID, chatID = null,myImage,myName;
    private Util util;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;
    private Permissions permissions;
    private static final String TAG = "MessageActivity";
    MessageAdapter messageAdapter ;
    ArrayList<MessageModel> messageModelArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_message, null, false);
        setContentView(binding.getRoot());
        // sharedPreferences for display myImage in Message Activity
        sharedPreferences = getSharedPreferences("UserData",MODE_PRIVATE);
        myImage = sharedPreferences.getString("userImage","");
        myName = sharedPreferences.getString("username", "");

        permissions = new Permissions();
        util = new Util();
        myID = util.getUID();
        //get extra  data from ChatFragment for display user image or get extra data from FirebaseNotificationService after user click on notification content
        if(getIntent().hasExtra("chatID")){
          // chatID = getIntent().getStringExtra("chatID");
            hisID = getIntent().getStringExtra("hisID");
            hisImage = getIntent().getStringExtra("hisImage");
            Log.d("message", "onCreate: hisID" + hisID + "\n myID" + myID);
           // readMessages(chatID);
        }
        else{
            // when i clicked on contact item ,contact adapter will send userId and userImage to Message Activity
            hisID = getIntent().getStringExtra("hisID");
            hisImage = getIntent().getStringExtra("hisImage");
        }

        binding.setImage(hisImage);
        binding.setActivity(this);
        messageModelArrayList = new ArrayList<>();
        messageAdapter  = new MessageAdapter(this,myImage,hisImage);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        binding.recyclerViewMessage.setLayoutManager(linearLayoutManager);
        binding.recyclerViewMessage.setAdapter(messageAdapter);
//       binding.recyclerViewMessage.smoothScrollToPosition(binding.recyclerViewMessage.getBottom());
        binding.recyclerViewMessage.setHasFixedSize(true);





//check if user chat exist then fetch id and read messages and we will create chatList then send message
        if (chatID == null)
            checkChat(hisID);

        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = binding.msgText.getText().toString().trim();
                if (message.isEmpty()) {
                    Toast.makeText(MessageActivity.this, "Enter Message...", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(message);
                    getToken(message,myID,myImage,chatID);
                }

                binding.msgText.setText("");
                // util.hideKeyBoard(MessageActivity.this);
            }
        });


        // updateTypingStatus in firebase for typing animation while user is typing
        binding.msgText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().length()==0){
                    updateTypingStatus("false");
                }
                else
                    updateTypingStatus(hisID);
                // updateTypingStatus(myID);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //for check online status
        checkStatus(hisID);




    }



    private void checkChat(final String hisID) {
        databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myID);
        Query query = databaseReference.orderByChild("member").equalTo(hisID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String id = ds.child("member").getValue().toString();
                        if (id.equals(hisID)) {
                            chatID = ds.getKey();
                            Log.d(TAG, "check chat:"+chatID);
                            readMessages(chatID);
                            //     Toast.makeText(MessageActivity.this, "read message", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(MessageActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //it is called in the first message
    private void createChat(String msg) {
        //create (myID)child which inside in ChatList
        databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myID);
        //create automatic key after adding myID and assigns it to chatID
        chatID = databaseReference.push().getKey();

        ChatListModel chatListModel = new ChatListModel(chatID, util.currentData(), msg, hisID);
        //upload chatID,currentData,msg,hisID to (chatID) child which inside in (myID) child
        databaseReference.child(chatID).setValue(chatListModel);

        //create (hisID)child which inside in ChatList
        databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(hisID);
        ChatListModel chatList = new ChatListModel(chatID, util.currentData(), msg, myID);
        //upload chatID,currentData,msg,hisID to (chatID) child which inside in (hisID) child
        databaseReference.child(chatID).setValue(chatList);

        //create (chatID)child which inside in Chat
        databaseReference = FirebaseDatabase.getInstance().getReference("Chat").child(chatID);
        MessageModel messageModel = new MessageModel(myID, hisID, msg, util.currentData(), "text");
        //generate automatic key inside in chatID and upload (myID,hisID,msg,currentData,type) to this automatic key
        databaseReference.push().setValue(messageModel);


        readMessages(chatID);
    }

    private void sendMessage(String msg) {
        if (chatID == null) {
            createChat(msg);
            Log.d(TAG, "create chat: ");
            // else (for example second message) after first message i can't generate new user chatList because it is already there
        } else {
            String date = util.currentData();
            Log.d(TAG, "send message: ");

            MessageModel messageModel = new MessageModel(myID, hisID, msg, date, "text");
            databaseReference = FirebaseDatabase.getInstance().getReference("Chat").child(chatID);
            //generate another automatic key inside in (chatID) which inside (chat) and upload (myID,hisID,msg,currentData,type) to this automatic key
            databaseReference.push().setValue(messageModel);

            Map<String, Object> map = new HashMap<>();
            map.put("lastMessage", msg);
            map.put("date", date);
            databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myID).child(chatID);
            //update only lastMessage and date in (chatID) which inside (myID) in (ChatList)
            databaseReference.updateChildren(map);

            databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(hisID).child(chatID);
            Map<String, Object> update = new HashMap<>();
            update.put("lastMessage", msg);
            update.put("date", date);
            //update only lastMessage and date in (chatID) which inside (hisID) in (ChatList)
            databaseReference.updateChildren(map);

        }
    }

    // when user clicks in icon info in toolbar which within message activity ,go to userInfo activity and display user info according userId
    public void userInfo() {
        Intent intent = new Intent(this, UserInfo.class);
        intent.putExtra("userID", hisID);
        startActivity(intent);
    }

    private void readMessages(String chatID) {
        FirebaseDatabase.getInstance().getReference().child("Chat").child(chatID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    messageModelArrayList.clear();
                    for (DataSnapshot dataSnapshot :snapshot.getChildren()){
                        MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                        messageModelArrayList.add(messageModel);
//                        messageAdapter.notifyDataSetChanged();

                        //for display last message in recycler view
//                        binding.recyclerViewMessage.smoothScrollToPosition(binding.recyclerViewMessage.getAdapter().getItemCount());
//                        binding.recyclerViewMessage.setItemViewCacheSize(binding.recyclerViewMessage.getAdapter().getItemCount());
                        Log.d(TAG, "readMessages: ");
                    }
                    messageAdapter.setMessageModelArrayList(messageModelArrayList);
                    messageAdapter.notifyItemInserted(messageModelArrayList.size());
                     // messageAdapter.notifyDataSetChanged();
                   binding.recyclerViewMessage.smoothScrollToPosition(binding.recyclerViewMessage.getAdapter().getItemCount()-1);

                    //Log.d(TAG, "item count :"+binding.recyclerViewMessage.getAdapter().getItemCount())
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessageActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }




    //for check status online in toolbar in Message Activity && show typing animation while user is typing
    private void checkStatus(String hisID) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(hisID);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String online = dataSnapshot.child("online").getValue().toString();
                    String typing = dataSnapshot.child("typing").getValue().toString();
                    binding.setStatus(online);
                    if (typing.equals(myID)) {
                        binding.typingStatus.setVisibility(View.VISIBLE);
                        Log.d(TAG, "onDataChange: ");
                        binding.typingStatus.playAnimation();
                    } else {
                        binding.typingStatus.cancelAnimation();
                        binding.typingStatus.setVisibility(View.INVISIBLE);
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MessageActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // updateTypingStatus in firebase for typing animation while user is typing
    private void updateTypingStatus(String status) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(myID);
        //  DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(hisID);
        Map<String, Object> map = new HashMap<>();
        map.put("typing", status);
        databaseReference.updateChildren(map);
    }
    // get token(device id) and other data for notification and make json object and put in it data(to-data) and pass this jsonObject(to) to sendNotification method for making http request for FCM by volley
    private void getToken(String message, String myID, String myImage, String chatID) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(hisID);
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String token = snapshot.child("token").getValue().toString();


                //this is the json format that FCM used it to send data message and every time i send data message because i want to generate custom notification
                JSONObject to = new JSONObject();
                JSONObject data = new JSONObject();
                try {
                    data.put("title", myName);
                    data.put("message", message);
                    data.put("hisID", myID);
                    data.put("hisImage", myImage);
                    data.put("chatID", chatID);


                    to.put("to", token);
                    to.put("data", data);

                    sendNotification(to);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(MessageActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // i will use volley library to send http request (JSONObject) to FCM Notification
    private void sendNotification(JSONObject to) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, AllConstants.NOTIFICATION_URL, to, response -> {
            Log.d("notification", "sendNotification: " + response);
        }, error -> {
            Log.d("notification", "sendNotification: " + error);
        }) {
            // in getHeaders method i will add my project server key for Authorization and application/type for Content-Type
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> map = new HashMap<>();
                map.put("Authorization", "key=" + AllConstants.SERVER_KEY);
                map.put("Content-Type", "application/json");
                return map;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        request.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }








    // i added it
//    @Override
//    protected void onStart() {
//       // firebaseRecyclerAdapter.startListening();
//        Toast.makeText(this, "on Start", Toast.LENGTH_SHORT).show();
//        super.onStart();
//    }
//    // i added it
//    @Override
//    protected void onStop() {
//        super.onStop();
//        firebaseRecyclerAdapter.stopListening();
//        Toast.makeText(this, "on Stop", Toast.LENGTH_SHORT).show();
//
//    }
//
    @Override
    protected void onResume() {
        util.updateOnlineStatus("online");
        super.onResume();
    }

//    @Override
//    protected void onStart() {
//        //for display always last message in recycler view
//        messageAdapter.notifyDataSetChanged();
//        binding.recyclerViewMessage.smoothScrollToPosition(binding.recyclerViewMessage.getAdapter().getItemCount());
//        super.onStart();
//    }

    @Override
    protected void onPause() {
        util.updateOnlineStatus(String.valueOf(System.currentTimeMillis()));
        super.onPause();
    }

}