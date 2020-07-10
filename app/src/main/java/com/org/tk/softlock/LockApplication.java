package com.org.tk.softlock;

import android.app.Application;
import android.content.Intent;

import com.org.tk.softlock.service.TaskMonitorService;

public class LockApplication extends Application {
    public static String softPre = "checkSoftPre";
    public static String softname = "checkSoftName";
    public static boolean isStopLock = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, TaskMonitorService.class);
        startService(intent);
    }
}
