package com.technion.doggyguide.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.technion.doggyguide.Chat.ChatActivity;
import com.technion.doggyguide.MainActivity;
import com.technion.doggyguide.R;
import com.technion.doggyguide.dataElements.DogOwnerElement;
import com.technion.doggyguide.users.UserProfile;

import java.util.Map;


public class FCMService extends FirebaseMessagingService {
    private String CHANNEL_ID = "Push Notifications";
    private Intent intent;
    private PendingIntent pendingIntent;
    String mDogOwners = "dogOwners";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection(mDogOwners);

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage payload) {
        super.onMessageReceived(payload);
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel mChannel;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.enableVibration(true);
            mChannel.enableLights(true);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mNotifyMgr.createNotificationChannel(mChannel);
        }

        switch (payload.getData().get("notification_type")) {

            case "CHAT":
                String user_id = payload.getData().get("sender_id");
                String status = payload.getData().get("user_status");
                String image = payload.getData().get("user_image");
                String name = payload.getData().get("user_name");
                intent = new Intent(this, ChatActivity.class);
                intent.putExtra("user_id", user_id);
                intent.putExtra("user_name", name);
                intent.putExtra("user_status", status);
                intent.putExtra("user_image", image);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
                break;
            case "POST":
                intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
                break;
            case "Friend_Req":
                String from_user_id_friend_req = payload.getData().get("sender_id");
                intent = new Intent(this, UserProfile.class);
                intent.putExtra("user_id", from_user_id_friend_req);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
                break;
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(payload.getData().get("title"))
                .setContentText(payload.getData().get("body"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        int mNotificationId = (int) System.currentTimeMillis();
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}