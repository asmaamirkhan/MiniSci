package com.asmaamir.minisci.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.asmaamir.minisci.R;
import com.asmaamir.minisci.activities.MainActivity;
import com.asmaamir.minisci.entities.Info;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Random;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationBroadcastReceiver";
    private static final String CHANNEL_ID = "com.asmaamir.minisci";
    private static final String CHANNEL_TITLE = "MiniSci notification channel";
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private FirebaseFirestore db;
    private Info info;

    @Override
    public void onReceive(Context context, Intent intent) {
        notificationBuilder = new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID);
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        db = FirebaseFirestore.getInstance();
        createNotificationChannel();
        showNotification(context);
        //Toast.makeText(context, "MiniSci alarm", Toast.LENGTH_LONG).show();
    }

    private void showNotification(Context context) {
        // info random ID interval: [1,1000]
        Random randGenerator = new Random();
        int random = randGenerator.nextInt(1000);
        db.collection("info").whereLessThanOrEqualTo("randomID", random).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Log.i(TAG, "size: " + queryDocumentSnapshots.size());
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        info = list.get(0).toObject(Info.class);
                        buildNotification(context,
                                info.getContent(),
                                "🧐 Bunu biliyor muydun?",
                                "MiniSci bilgilendiricisi"
                        );
                        mNotificationManager.notify(0, notificationBuilder.build());
                        Log.i(TAG, "Notification is created");
                    }
                });

    }

    private void buildNotification(Context context, String bigText, String bigContentTitle, String summaryText) {
        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(bigText);
        bigTextStyle.setBigContentTitle(bigContentTitle);
        bigTextStyle.setSummaryText(summaryText);

        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setSmallIcon(R.drawable.ic_baseline_star_24);
        //notificationBuilder.setContentTitle(contentTitle);
        //notificationBuilder.setContentText(contentText);
        notificationBuilder.setStyle(bigTextStyle);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_TITLE,
                NotificationManager.IMPORTANCE_HIGH);
        mNotificationManager.createNotificationChannel(channel);
        notificationBuilder.setChannelId(CHANNEL_ID);
    }

}