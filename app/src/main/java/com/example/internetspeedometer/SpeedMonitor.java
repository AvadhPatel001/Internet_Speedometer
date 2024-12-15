package com.example.internetspeedometer;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

public class SpeedMonitor extends Service {

    private static final String CHANNEL_ID = "InternetSpeedChannel";
    private long previousRxBytes = 0;
    private long previousTxBytes = 0;
    private Handler handler = new Handler();


    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        previousRxBytes = TrafficStats.getTotalRxBytes();
        previousTxBytes = TrafficStats.getTotalTxBytes();

        handler.post(updateSpeedRunnable);
        return START_STICKY;
    }

    private Runnable updateSpeedRunnable = new Runnable() {
        @Override
        public void run() {
            long currentRxBytes = TrafficStats.getTotalRxBytes();
            long currentTxBytes = TrafficStats.getTotalTxBytes();

            long downloadedBytes = currentRxBytes - previousRxBytes;
            long uploadedBytes = currentTxBytes - previousTxBytes;

            previousRxBytes = currentRxBytes;
            previousTxBytes = currentTxBytes;

            String download_speed = formatSpeed(downloadedBytes).replace("\n", " ");
            String upload_speed = formatSpeed(uploadedBytes).replace("\n", " ");

            long total_bytes = downloadedBytes + uploadedBytes;
            String total_speed = formatSpeed(total_bytes);
            String total_Title = total_speed.replace("\n", " ");

            String notificationText = "Download: " + download_speed + " | Upload: " + upload_speed ;

            TextView textView = new TextView(SpeedMonitor.this);

            SpannableString display_str_span = new SpannableString(total_speed);
            display_str_span.setSpan(new AbsoluteSizeSpan(130),0,total_speed.length() - 4,0);
            display_str_span.setSpan(new AbsoluteSizeSpan(90),total_speed.length() - 4 ,total_speed.length(),0);

            textView.setText(display_str_span);
            textView.setTextColor(Color.WHITE);
            textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            textView.setGravity(Gravity.CENTER);
            textView.setLineSpacing(0.5f, 0.8f);
            textView.setLetterSpacing(-0.04f);
            textView.setPadding(0,-25,0,0);

            textView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());

            Bitmap bitmap = Bitmap.createBitmap(205,
                    195, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            textView.draw(canvas);

            Notification builder = new NotificationCompat.Builder(SpeedMonitor.this, CHANNEL_ID)
                    .setContentTitle(total_Title)
                    .setContentText(notificationText)
                    .setSmallIcon(IconCompat.createWithBitmap(bitmap))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .build();

            startForeground(1,builder);

            handler.postDelayed(this, 1000);

        }
    };

    @SuppressLint("DefaultLocale")
    private String formatSpeed(long bytesPerSecond) {
        double kbps = bytesPerSecond / 1024.0;
        String formated;
        if (kbps < 1024) {
            formated = String.format("%.0f\nKB/s", kbps);
            return formated;
        } else if ((kbps / 1024) < 9.9){
            formated = String.format("%.1f\nMB/s",(kbps / 1024));
            return formated;
        }else {
            formated = String.format("%.0f\nMB/s",(kbps / 1024));
            return formated;
        }
    }


    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Internet Speed Notifications",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateSpeedRunnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
