package com.example.finaltask.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.app.NotificationManagerCompat;

public class DismissReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String documentId = intent.getStringExtra("documentId");
        int notificationId = documentId.hashCode();

        // Dừng âm thanh
        AlarmReceiver.stopAlarm();

        // Hủy thông báo
        NotificationManagerCompat.from(context).cancel(notificationId);
    }
}