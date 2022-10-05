package com.remote.silvercare.ui.home;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.remote.silvercare.AlarmReceiver;
import com.remote.silvercare.R;

import java.util.Calendar;

public class MoveDetectService extends Service {

    public static Intent serviceIntent = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceIntent = intent;

        // TODO
        for(int i = 0; i < 9999; i ++)
        {
            Log.i("MoveDetectorService", "실행중");
        }
        initializeNotification();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initializeNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1");
        builder.setSmallIcon(R.mipmap.ic_launcher);

        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();

        style.bigText("움직임 모니터링 서비스 동작중");
        style.setBigContentTitle(null);

//        SharedPreferences phoneNumber = getSharedPreferences("phoneNumber", Activity.MODE_PRIVATE);
        style.setSummaryText("서비스 실행중");

        Log.i("Address: ", "움직임 감지 서비스 실행중");

        builder.setContentText(null);
        builder.setContentTitle(null);
        builder.setOngoing(true);

        builder.setStyle(style);
        builder.setWhen(0);
        builder.setShowWhen(false);

        Intent notificationIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:119"));
        PendingIntent pendingIntent;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE);
        }else{
            pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            manager.createNotificationChannel(new NotificationChannel("1", "MoveDetectService", NotificationManager.IMPORTANCE_NONE));
        }

        Notification notification = builder.build();
        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        final Calendar calender = Calendar.getInstance();
        calender.setTimeInMillis(System.currentTimeMillis());
        calender.add(Calendar.SECOND, 3);
        Intent intent = new Intent(this, MoveDetectorAlarmReceiver.class);

        PendingIntent sender;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            sender = PendingIntent.getBroadcast(this,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        }else {
            sender = PendingIntent.getActivity(this,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), 1000 * 60 * 15 , sender);
    }
}
