package com.example.zemochat.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.zemochat.Constants.AllConstants;
import com.example.zemochat.R;
import com.example.zemochat.UserModel;
import com.example.zemochat.databinding.FragmentVerifyNumberBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class VerifyNumber extends Fragment {
    private FragmentVerifyNumberBinding binding;
    private String OTP, pin;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;


    public VerifyNumber() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVerifyNumberBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        Bundle bundle = getArguments();


        if (bundle != null) {
            OTP = bundle.getString(AllConstants.VERIFICATION_CODE);
        }

        binding.btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPin();
                if (checkPin()) {
                    binding.progressLayout.setVisibility(View.VISIBLE);
                    binding.progressBar.start();

                    verifyPin(pin);
                }
            }
        });

        return view;
    }


    private boolean checkPin() {

        pin = binding.otpTextView.getText().toString();
        if (TextUtils.isEmpty(pin)) {
            binding.otpTextView.setError("Enter the pin");
            return false;
        } else if (pin.length() < 6) {
            binding.otpTextView.setError("Enter valid pin");
            return false;
        } else {
            binding.otpTextView.setError(null);
            return true;
        }
    }

    private void verifyPin(String pin) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(OTP, pin);
        signInWithPhoneAuthCredential(credential);
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                        UserModel userModel = new UserModel("", "", "", firebaseAuth.getCurrentUser().getPhoneNumber(),
                                firebaseAuth.getUid(), "online", "false");
                        databaseReference.child(firebaseAuth.getUid()).setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    getFragmentManager().beginTransaction().replace(R.id.container, new UserData()).commit();
                                    binding.progressLayout.setVisibility(View.GONE);
                                    binding.progressBar.stop();

                                } else
                                    Toast.makeText(getContext(), "" + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        });


                } else
                    Toast.makeText(getContext(), "" + task.getResult(), Toast.LENGTH_SHORT).show();
            }
        });
    }


//
//    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
//
//
//        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if (task.isSuccessful()) {
//
//                    // i will use it for getting mobile device id (token) and i will using this token for FCM (notification)
//                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(data -> {
//
//                        String token = data.getResult().getToken();
//                        UserModel userModel = new UserModel("", "", "", firebaseAuth.getCurrentUser().getPhoneNumber(),
//                                firebaseAuth.getUid(), "online", "false",token);
//                        databaseReference.child(firebaseAuth.getUid()).setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if (task.isSuccessful()) {
//
//                                    getFragmentManager().beginTransaction().replace(R.id.container, new UserData()).commit();
//                                    binding.progressLayout.setVisibility(View.GONE);
//                                    binding.progressBar.stop();
//
//                                } else
//                                    Toast.makeText(getContext(), "" + task.getException(), Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    });
//
//                } else
//                    Toast.makeText(getContext(), "" + task.getResult(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
//
//
//        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if (task.isSuccessful()) {
//                    databaseReference.child(firebaseAuth.getUid()).child("number").setValue(firebaseAuth.getCurrentUser().getPhoneNumber()+"");
//                    Fragment fragment = new UserData();
//                    getFragmentManager().beginTransaction()
//                            .replace(R.id.container, fragment)
//                            .commit();
//
//                } else
//                    Toast.makeText(getContext(), task.getException() + "", Toast.LENGTH_SHORT).show();
//
//            }
//        });
//    }
}
