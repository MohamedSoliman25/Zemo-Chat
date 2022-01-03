package com.example.zemochat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.example.zemochat.R;
import com.example.zemochat.UserModel;
import com.example.zemochat.Utils.Util;
import com.example.zemochat.databinding.ActivityUserInfoBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class UserInfo extends AppCompatActivity {

    private ActivityUserInfoBinding binding;
    private Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        util = new Util();

        String uID = getIntent().getStringExtra("userID");

        getUserDetail(uID);
    }

    private void getUserDetail(String uID) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uID);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
                    binding.setUserModel(userModel);

                    if (userModel.getName().contains(" ")) {
                        String[] split = userModel.getName().split(" ");
                        binding.txtProfileFName.setText(split[0]);
                        binding.txtProfileLName.setText(split[1]);
                    } else {
                        binding.txtProfileFName.setText(userModel.getName());
                        binding.txtProfileLName.setText("");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(UserInfo.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //for action on back icon
    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return super.onSupportNavigateUp();
    }

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