package com.example.zemochat.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.example.zemochat.Constants.AllConstants;
import com.example.zemochat.MessageActivity;
import com.example.zemochat.R;
import com.example.zemochat.Utils.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class FirebaseNotificationService extends FirebaseMessagingService {

    private Util util = new Util();

    Bitmap bitmap;

    //receive data message and show notification
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            Map<String, String> map = remoteMessage.getData();
            String title = map.get("title");
            String message = map.get("message");
            String hisID = map.get("hisID");
            String hisImage = map.get("hisImage");
            String chatID = map.get("chatID");

            Log.d("TAG", "onMessageReceived: chatID is " + chatID + "\n hisID" + hisID);

       //     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
          //      createOreoNotification(title, message, hisID, hisImage, chatID);
         //   else
                createNormalNotification(title, message, hisID, hisImage, chatID);
        }
        else Log.d("TAG", "onMessageReceived: no data ");


        super.onMessageReceived(remoteMessage);
    }

    //this is method is called when new token is change or generate then i update that token in user data in firebase
    @Override
    public void onNewToken(@NonNull String s) {
        updateToken(s);
        super.onNewToken(s);
    }

    private void updateToken(String token) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//note: token is a device id
        if (firebaseAuth.getCurrentUser() != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(util.getUID());
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            databaseReference.updateChildren(map);
        }

    }


    private void createNormalNotification(String title, String message, String hisID, String hisImage, String chatID) {

        //if my phone sdk >=26 create notification channel
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(AllConstants.CHANNEL_ID, "Message", NotificationManager.IMPORTANCE_HIGH);
            channel.setShowBadge(true);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
        //Build notification
        //for sound of notification
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        try {
            bitmap =  Glide.with(this).asBitmap().load(hisImage).submit().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, AllConstants.CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(bitmap)
                .setAutoCancel(true)
                .setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null))
                .setSound(uri);
// i use this intent to open activity when user click on notification content
        Intent intent = new Intent(this, MessageActivity.class);
        //i use this flag for clear all previous activity after user press on notification
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // i add putExtra data for use this data in MessageActivity after user click on notification content
        intent.putExtra("chatID", chatID);
        intent.putExtra("hisID", hisID);
        intent.putExtra("hisImage", hisImage);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // i use random class for generate a random id of notification for every notification
        manager.notify(new Random().nextInt(85 - 65), builder.build());

    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void createOreoNotification(String title, String message, String hisID, String hisImage, String chatID) {
//
//        NotificationChannel channel = new NotificationChannel(AllConstants.CHANNEL_ID, "Message", NotificationManager.IMPORTANCE_HIGH);
//        channel.setShowBadge(true);
//        channel.enableLights(true);
//        channel.enableVibration(true);
//        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
//
//        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        manager.createNotificationChannel(channel);
//
//        Intent intent = new Intent(this, MessageActivity.class);
//        //i use this flag for clear all previous activity after user press on notification
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        // i use this intent to open activity when user click on notification content
//        intent.putExtra("hisID", hisID);
//        intent.putExtra("hisImage", hisImage);
//        intent.putExtra("chatID", chatID);
//
//
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//
//        Notification notification = new Notification.Builder(this, AllConstants.CHANNEL_ID)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null))
//                .setSmallIcon(R.drawable.ic_launcher_foreground)
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true)
//                .build();
//        manager.notify(new Random().nextInt(85 - 65), notification);
//    }
}
