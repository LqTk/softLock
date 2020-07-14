package com.org.tk.softlock.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.org.tk.softlock.LockApplication;
import com.org.tk.softlock.service.TaskMonitorService;

public class BattaryBroadCast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isRun = LockApplication.isServiceRunning(context, TaskMonitorService.class.getName());
        Log.d("MontationIsRunning","="+isRun);
        Toast.makeText(context,"电量改变",Toast.LENGTH_SHORT).show();
        if (!isRun){
            Intent intent1 = new Intent(context, TaskMonitorService.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(intent1);
        }
    }
}
