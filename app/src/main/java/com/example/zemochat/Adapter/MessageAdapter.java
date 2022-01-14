package com.example.zemochat.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zemochat.BR;
import com.example.zemochat.MessageActivity;
import com.example.zemochat.MessageModel;
import com.example.zemochat.Utils.Util;
import com.example.zemochat.databinding.LeftItemLayoutBinding;
import com.example.zemochat.databinding.RightItemLayoutBinding;

import java.util.ArrayList;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    Context context;
    ArrayList<MessageModel> messageModelArrayList ;
    String myImage,hisImage;
    Util util = new Util();

    public MessageAdapter(Context context, ArrayList<MessageModel> messageModelArrayList, String myImage, String hisImage) {
        this.context = context;
        this.messageModelArrayList = messageModelArrayList;
        this.myImage = myImage;
        this.hisImage = hisImage;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==0){
            ViewDataBinding viewDataBinding = RightItemLayoutBinding.inflate(LayoutInflater.from(context),parent,false);
            return new MessageViewHolder(viewDataBinding);
        }
        else{
            ViewDataBinding viewDataBinding = LeftItemLayoutBinding.inflate(LayoutInflater.from(context),parent,false);
            return new MessageViewHolder(viewDataBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        // handle set mine image
        if(getItemViewType(position) == 0){
            //display sender image(my image)
            holder.viewDataBinding.setVariable(BR.messageImage,myImage);
            holder.viewDataBinding.setVariable(BR.message,messageModelArrayList.get(position));
        }
        else{
            holder.viewDataBinding.setVariable(BR.messageImage,hisImage);
            holder.viewDataBinding.setVariable(BR.message,messageModelArrayList.get(position));
        }
//        Log.d(TAG, "get item count : "+getItemCount());
    }

    @Override
    public int getItemCount() {
        return messageModelArrayList.size();
    }
    //getItemViewType is used to check wheather layout it sender layout or receiver layout this is the way we use multi layouts in adapter class
    @Override
    public int getItemViewType(int position) {
        MessageModel messageModel = messageModelArrayList.get(position);
        if (util.getUID().equals(messageModel.getSender()))
            return 0;
        else
            return 1;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        //ViewDataBinding is general class of any binding layout
        private ViewDataBinding viewDataBinding;

        public MessageViewHolder(@NonNull ViewDataBinding viewDataBinding) {
            super(viewDataBinding.getRoot());
            this.viewDataBinding = viewDataBinding;

        }
    }
}
