package com.example.finaltask.notification;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.finaltask.MainActivity;
import com.example.finaltask.R;

// ... giữ nguyên package và import

public class AlarmReceiver extends BroadcastReceiver {
    private static MediaPlayer mediaPlayer;

    @Override
    public void onReceive(Context context, Intent intent) {
        new NotificationHelper(context); // Tạo channel nếu chưa có

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("AlarmReceiver", "Không có quyền thông báo");
                return;
            }
        }

        String taskName = intent.getStringExtra("taskName");
        String documentId = intent.getStringExtra("documentId");

        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        Intent dismissIntent = new Intent(context, DismissReceiver.class);
        dismissIntent.putExtra("documentId", documentId);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                documentId.hashCode(),
                dismissIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(context, "task_notifications_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Đến giờ: " + taskName)
                .setContentText("Hãy kiểm tra công việc!")
                .addAction(R.drawable.ic_close, "TẮT", dismissPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat.from(context).notify(documentId.hashCode(), notification);
        Log.d("AlarmReceiver", "Đã gửi thông báo: " + taskName);

        playAlarmSound(context);
    }

    private void playAlarmSound(Context context) {
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        try {
            mediaPlayer = MediaPlayer.create(context, alarmUri);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopAlarm() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d("AlarmReceiver", "Âm thanh đã dừng");
        }
    }
}
