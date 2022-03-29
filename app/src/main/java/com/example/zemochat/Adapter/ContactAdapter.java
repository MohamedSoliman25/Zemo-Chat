package com.example.zemochat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;


import com.example.zemochat.Activity.UserInfo;
import com.example.zemochat.MessageActivity;
import com.example.zemochat.R;
import com.example.zemochat.UserModel;
import com.example.zemochat.databinding.ContactItemLayoutBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<com.example.zemochat.Adapter.ContactAdapter.ViewHolder> implements Filterable {

    private Context context;
    private ArrayList<UserModel> arrayList = new ArrayList<>();
    private ArrayList<UserModel> filterArrayList = new ArrayList<>();
    private ContactItemLayoutBinding binding;
    private static final String TAG = "ContactAdapter";

    public ContactAdapter(Context context) {
        this.context = context;
//        this.arrayList = arrayList;
//        filterArrayList = new ArrayList<>();

    }

    public void setArrayList(ArrayList<UserModel> arrayList) {
        this.arrayList = arrayList;
        //add original list to filter array list by default
        filterArrayList.clear();
        filterArrayList.addAll(arrayList);
        notifyDataSetChanged();
      //  Log.d(TAG, "mosetArrayList: "+arrayList.get(0).getName());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.contact_item_layout, parent, false);


        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final UserModel userModel = arrayList.get(position);
        holder.layoutBinding.setUserModel(userModel);

        //if user press on icon info i pass user id and i will display user profile
        holder.layoutBinding.imgContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserInfo.class);
                intent.putExtra("userID", userModel.getuID());
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("hisID", userModel.getuID());
                intent.putExtra("hisImage", userModel.getImage());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList == null ? 0 : arrayList.size();
    }

    // when user is writing his search contact name the  onQueryTextChange is called and i called this method
    @Override
    public Filter getFilter() {
        return contactFilter;
    }
//after getFilter is called the onQueryTextChange is pass newtext(user search) to performFiltering method
    private Filter contactFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {


           // Log.d(TAG, "performFiltering:"+constraint);
            List<UserModel> filteredList = new ArrayList<>();
            arrayList.clear();
            //if user does not write anything in search , by default i display all contacts in my app by adding filterArrayList to filteredList
            if (constraint == null || constraint.length() == 0)
                filteredList.addAll(filterArrayList);
            else {
                // take filter (user search) and convert name of contact to lower case
                String filter = constraint.toString().toLowerCase().trim();
                for (UserModel userModel : filterArrayList) {
                   // Log.d(TAG, "userModel: "+userModel.getName());
                // check if name in the filterArrayList is contains filter(user search ), if true add user model to filteredList
                    if (userModel.getName().toLowerCase().contains(filter)) {
                        filteredList.add(userModel);
                       // Log.d(TAG, "get search : "+filteredList.get());
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        // after adding (user search to) filteredList , add filteredList to arrayList (it is meaning if user write on user in search someone ,the arrayList display this person )
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            arrayList.clear();
            arrayList.addAll((Collection<? extends UserModel>) results.values);
            notifyDataSetChanged();

        }
    };


    public class ViewHolder extends RecyclerView.ViewHolder {
        ContactItemLayoutBinding layoutBinding;

        public ViewHolder(@NotNull ContactItemLayoutBinding layoutBinding) {
            super(layoutBinding.getRoot());
            this.layoutBinding = layoutBinding;
        }
    }



}
