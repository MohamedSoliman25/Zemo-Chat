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

import com.example.zemochat.Activity.UserInfo;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MessageActivity extends AppCompatActivity {
    private ActivityMessageBinding binding;
    private String hisID, hisImage, myID, chatID = null,myImage;
    private Util util;
    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter<MessageModel, ViewHolder> firebaseRecyclerAdapter;
    private SharedPreferences sharedPreferences;
    private Permissions permissions;
    private static final String TAG = "MessageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_message, null, false);
        setContentView(binding.getRoot());
        // sharedPreferences for display myImage in Message Activity
        sharedPreferences = getSharedPreferences("UserData",MODE_PRIVATE);
        myImage = sharedPreferences.getString("userImage","");

        permissions = new Permissions();
        util = new Util();
        myID = util.getUID();
        //get extra  data from ChatFragment for display user image
        if(getIntent().hasExtra("chatID")){
            chatID = getIntent().getStringExtra("chatID");
            hisID = getIntent().getStringExtra("hisID");
            hisImage = getIntent().getStringExtra("hisImage");
            Log.d("message", "onCreate: hisID" + hisID + "\n myID" + myID);
            readMessages(chatID);
        }
       else{
            // when i clicked on contact item ,contact adapter will send userId and userImage to Message Activity
            hisID = getIntent().getStringExtra("hisID");
            hisImage = getIntent().getStringExtra("hisImage");
       }



        binding.setImage(hisImage);
        binding.setActivity(this);



//check if user chat exist then fetch id else we will create chatList then send message
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
                }

                binding.msgText.setText("");
                util.hideKeyBoard(MessageActivity.this);
            }
        });


        //for check online status
        checkStatus(hisID);

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
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });




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
                            Toast.makeText(MessageActivity.this, "read message", Toast.LENGTH_SHORT).show();
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

            // else (for example second message) after first message i can't generate new user chatList because it is already there
        } else {
            String date = util.currentData();

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
        Query query = FirebaseDatabase
                .getInstance().getReference().child("Chat")
                .child(chatID);
        FirebaseRecyclerOptions<MessageModel> options = new FirebaseRecyclerOptions.Builder<MessageModel>()
                .setQuery(query, MessageModel.class).build();
        query.keepSynced(true);

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<MessageModel, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int i, @NonNull MessageModel messageModel) {

                // handle set mine image
                if(getItemViewType(i) == 0){
                    //display sender image(my image)
                    viewHolder.viewDataBinding.setVariable(BR.messageImage,myImage);
                    viewHolder.viewDataBinding.setVariable(BR.message,messageModel);
                }
                else{
                    viewHolder.viewDataBinding.setVariable(BR.messageImage,hisImage);
                    viewHolder.viewDataBinding.setVariable(BR.message,messageModel);
                }



            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                if (viewType==0){
                    ViewDataBinding viewDataBinding = RightItemLayoutBinding.inflate(LayoutInflater.from(getBaseContext()),parent,false);
                    return new ViewHolder(viewDataBinding);
                }
                else{
                    ViewDataBinding viewDataBinding = LeftItemLayoutBinding.inflate(LayoutInflater.from(getBaseContext()),parent,false);
                    return new ViewHolder(viewDataBinding);
                }
            }

            //getItemViewType is used to check wheather layout it sender layout or receiver layout this is the way we use multi layouts in adapter class
            @Override
            public int getItemViewType(int position) {
                MessageModel messageModel = getItem(position);
                if (myID.equals(messageModel.getSender()))
                    return 0;
                else
                    return 1;
            }
        };
        binding.recyclerViewMessage.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewMessage.setAdapter(firebaseRecyclerAdapter);
        // i modified it from false to true
        binding.recyclerViewMessage.setHasFixedSize(true);
        firebaseRecyclerAdapter.startListening();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        //ViewDataBinding is general class of any binding layout
        private ViewDataBinding viewDataBinding;

        public ViewHolder(@NonNull ViewDataBinding viewDataBinding) {
            super(viewDataBinding.getRoot());
            this.viewDataBinding = viewDataBinding;

        }
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
                        binding.typingStatus.playAnimation();
                    } else {
                        binding.typingStatus.cancelAnimation();
                        binding.typingStatus.setVisibility(View.GONE);
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
        Map<String, Object> map = new HashMap<>();
        map.put("typing", status);
        databaseReference.updateChildren(map);
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

    @Override
    protected void onPause() {
        util.updateOnlineStatus(String.valueOf(System.currentTimeMillis()));
        super.onPause();
    }

}