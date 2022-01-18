package com.example.zemochat.Fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.zemochat.Adapter.ContactAdapter;
import com.example.zemochat.Constants.AllConstants;
import com.example.zemochat.Permissions.Permissions;
import com.example.zemochat.R;
import com.example.zemochat.UserModel;
import com.example.zemochat.Utils.Util;
import com.example.zemochat.databinding.FragmentContactBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ContactFragment extends Fragment implements SearchView.OnQueryTextListener {

    private FragmentContactBinding binding;
    private DatabaseReference databaseReference;
    private Permissions permissions;
    private ArrayList<UserModel> userContacts, appContacts;
    private ContactAdapter contactAdapter;
    private String userPhoneNumber;
    private static final String TAG = "ContactFragment";
    private Util util;


    public ContactFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentContactBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        permissions = new Permissions();
        binding.recyclerViewContact.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewContact.setHasFixedSize(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        appContacts = new ArrayList<>();
        contactAdapter = new ContactAdapter(getActivity());
        binding.recyclerViewContact.setAdapter(contactAdapter);
       // userPhoneNumber = firebaseAuth.getCurrentUser().getDisplayName();
        //my phone
        userPhoneNumber = firebaseAuth.getCurrentUser().getPhoneNumber();


        getUserContacts();
//for search about my contacts in my app
        binding.contactSearchView.setOnQueryTextListener(this);
        return view;
    }



    // for getting all contacts phone number&name  of user phone and save it to userContacts list
    private void getUserContacts() {

//check read  contacts permission
        if (permissions.isContactOk(getContext())) {
            userContacts = new ArrayList<>();
            String[] projection = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            };
            ContentResolver cr = getContext().getContentResolver();
            Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null);
            if (cursor != null) {
                userContacts.clear();
                try {


                    while (cursor.moveToNext()) {

                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        // if number has white spaces then all spaces remove in this line
                        number = number.replaceAll("\\s", "");

                        //if number start at 0 because our number save in +20 or other country code so we remove 0 at index 0 in this line
                        String num = String.valueOf(number.charAt(0));
                        if (num.equals("0"))
                            number = number.replaceFirst("(?:0)+", "+20");
                        Log.d(TAG, "getUserContacts:"+name+":"+number);

                        UserModel userModel = new UserModel();
                        userModel.setName(name);
                        userModel.setNumber(number);
                        userContacts.add(userModel);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            cursor.close();
            getAppContacts(userContacts);

        }
        else permissions.requestContact(getActivity());
    }

    // for check which user use my app and if user use my app , shows user list in contacts
    private void getAppContacts(final ArrayList<UserModel> mobileContacts) {


        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        //order database data according to child "number"
        Query query = databaseReference.orderByChild("number");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                   appContacts.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String number = ds.child("number").getValue().toString();

                        for (UserModel userModel : mobileContacts) {
//check if user mobile in contacts is equal to user mobile in my app && user mobile in my app is not equal to my mobile
                            if (userModel.getNumber().equals(number)&&!number.equals(userPhoneNumber)) {

                                String image = ds.child("image").getValue().toString();
                                String status = ds.child("status").getValue().toString();
                                String uID = ds.child("uID").getValue().toString();

                                String name = ds.child("name").getValue().toString();
                                UserModel registeredUser = new UserModel();
                                registeredUser.setName(name);
                                registeredUser.setStatus(status);
                                registeredUser.setImage(image);
                                registeredUser.setuID(uID);
                               // Log.d(TAG, "onDataChange for num : my mob : "+number+"    ,  contacts mob : "+userModel.getNumber());

                                appContacts.add(registeredUser);
                                break;
                            }
                        }
                    }
                    contactAdapter.setArrayList(appContacts);



                } else Toast.makeText(getContext(), "No Data Found", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AllConstants.CONTACTS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getUserContacts();
                } else
                    Toast.makeText(getContext(), "Contact Permission denied", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (contactAdapter != null){
            Log.d(TAG, "onQueryTextChange:"+newText);
            contactAdapter.getFilter().filter(newText);
            }
        return false;
    }

//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(getView().getWindowToken(),0);
//    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(getView().getWindowToken(),0);
//        Toast.makeText(getContext(), "onResume", Toast.LENGTH_SHORT).show();
//    }

    @Override
    public void onPause() {
        super.onPause();
        util.hideKeyBoard(getActivity());
    }
}