package com.example.finaltask.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.example.finaltask.R;

public class NotificationHelper {
    private final Context mContext;
    private static final String CHANNEL_ID = "task_notifications_channel";
    private static final String CHANNEL_NAME = "Task Notifications";
    private static final String CHANNEL_DESCRIPTION = "Channel for task reminders";

    public NotificationHelper(@NonNull Context context) {
        this.mContext = context;
        createNotificationChannel(); // Luôn gọi tạo channel khi khởi tạo
    }

    /**
     * Tạo notification channel cho Android 8.0+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = mContext.getSystemService(NotificationManager.class);

            // Kiểm tra nếu channel đã tồn tại
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription(CHANNEL_DESCRIPTION);
                channel.enableVibration(true);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

                manager.createNotificationChannel(channel);
                Log.d("NotificationHelper", "Channel created: " + CHANNEL_ID);
            }
        }
    }

    /**
     * Gửi thông báo với tiêu đề và nội dung
     */
    public void sendNotification(@NonNull String title, @NonNull String message) {
        // Kiểm tra quyền trên Android 13+
        if (!hasNotificationPermission()) {
            Log.e("NotificationHelper", "Không có quyền gửi thông báo");
            return;
        }

        Notification notification = buildNotification(title, message);
        NotificationManagerCompat.from(mContext).notify(generateNotificationId(), notification);
    }

    /**
     * Kiểm tra quyền thông báo trên Android 13+
     */
    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    mContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Dưới Android 13 không cần quyền
    }

    /**
     * Xây dựng notification
     */
    private Notification buildNotification(String title, String message) {
        return new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background) // Sử dụng icon hợp lệ
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
    }

    /**
     * Tạo ID thông báo duy nhất
     */
    private int generateNotificationId() {
        return (int) System.currentTimeMillis();
    }
}