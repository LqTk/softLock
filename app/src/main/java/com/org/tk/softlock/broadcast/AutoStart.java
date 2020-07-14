package com.org.tk.softlock.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.org.tk.softlock.activity.MainActivity;
import com.org.tk.softlock.service.TaskMonitorService;

public class AutoStart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("开机了。。","startService");
        Intent intent1 = new Intent(context, TaskMonitorService.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(intent1);
    }
}
