package com.org.tk.softlock.service;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.org.tk.softlock.LockApplication;
import com.org.tk.softlock.activity.InputNumPswLockActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TaskMonitorService extends Service {
    private boolean isStart = false;
    private SharedPreferences preferences;
    private String hasSoftName;
    private Timer timer;
    private String startPackageName="";

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, final int flags, int startId) {
        if (timer==null){
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    preferences = getSharedPreferences(LockApplication.softPre,MODE_PRIVATE);
                    hasSoftName = preferences.getString(LockApplication.softname, "");
                    List<String> name = new ArrayList<>();
                    String[] hasName = hasSoftName.split(",");
                    for (int i=0;i<hasName.length;i++){
                        if (!TextUtils.isEmpty(hasName[i])) {
                            name.add(hasName[i].replaceAll(" ",""));
                        }
                    }
                    String packageName="";
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
                        packageName = getRunningApp();
                    }else {
                        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                        List<ActivityManager.RunningTaskInfo> taskInfos = am.getRunningTasks(1);
                        packageName = taskInfos.get(0).topActivity.getPackageName();
                    }
                    Log.d("TopTaskName=",packageName);
                    if (LockApplication.isStopLock && !TextUtils.isEmpty(packageName) && !startPackageName.equals(packageName)){
                        LockApplication.isStopLock = false;
                    }
                    if (isStart && !TextUtils.isEmpty(packageName) && !startPackageName.equals(packageName) && !packageName.equals("com.org.tk.softlock")){
                        isStart = false;
                    }
                    String passwordStr = preferences.getString(LockApplication.softpassword, "");
                    if (name.contains(packageName) && !isStart && !TextUtils.isEmpty(passwordStr)){
                        if (!LockApplication.isStopLock){
                            Intent intent = new Intent(getBaseContext(), InputNumPswLockActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("packageName",packageName);
                            startActivity(intent);
                            isStart = true;
                            startPackageName = packageName;
                        }
                    }
                }
            },0,500);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private String getRunningApp() {
        long ts = System.currentTimeMillis();
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,ts-2000, ts);
        if (queryUsageStats == null || queryUsageStats.isEmpty()) {
            return "";
        }
        UsageStats recentStats = null;
        for (UsageStats usageStats : queryUsageStats) {
            if (recentStats == null ||
                    recentStats.getLastTimeUsed() < usageStats.getLastTimeUsed()) {
                recentStats = usageStats;
            }
        }
        return recentStats.getPackageName();
    }

}
