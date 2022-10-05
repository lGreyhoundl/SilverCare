package com.remote.silvercare.ui.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.remote.silvercare.RebootReceiver;
import com.remote.silvercare.UndeadService;

public class MoveDetectorRebootReceiver extends BroadcastReceiver {

    public MoveDetectorRebootReceiver(){}

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent in = new Intent(context, MoveDetectorRestarter.class);
            context.startForegroundService(in);
        } else {
            Intent in = new Intent(context, MoveDetectService.class);
            context.startService(in);
        }
    }
}
