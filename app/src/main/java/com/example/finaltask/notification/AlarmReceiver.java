package com.example.finaltask.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;

import com.example.finaltask.MainActivity;
import com.example.finaltask.R;

public class AlarmReceiver extends BroadcastReceiver {

    private static MediaPlayer mediaPlayer;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Kiểm tra quyền thông báo trên Android 13 trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d("AlarmReceiver", "Permission not granted for notifications");
                return;  // Nếu quyền không được cấp, không gửi thông báo
            }
        }

        // Tạo Intent để mở ứng dụng khi người dùng nhấn vào thông báo
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Tạo notification
        Notification notification = new NotificationCompat.Builder(context, "task_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Đã đến giờ công việc!")
                .setContentText("Đây là thông báo về công việc của bạn.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        // Hiển thị notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(0, notification);
        }

        // Phát chuông báo lặp lại
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        try {
            // Khởi tạo MediaPlayer để phát âm thanh báo thức
            mediaPlayer = MediaPlayer.create(context, alarmUri);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true); // Bật lặp lại âm thanh
                mediaPlayer.start();
                Log.d("AlarmReceiver", "MediaPlayer started!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Đảm bảo dừng âm thanh khi ứng dụng bị đóng hoặc khi không còn cần thiết
    public static void stopAlarm() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            Log.d("AlarmReceiver", "MediaPlayer stopped!");
        }
    }
}