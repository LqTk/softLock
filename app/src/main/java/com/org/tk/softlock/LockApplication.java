package com.org.tk.softlock;

import android.app.ActivityManager;
import android.app.Application;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.org.tk.softlock.service.TaskMonitorService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class LockApplication extends Application {
    public static String softPre = "checkSoftPre";
    public static String softname = "checkSoftName";
    public static String softpassword = "passwordSoft";
    public static String softquestion = "passwordquestion";
    public static String lastTime = "lasttime";
    public static String lastError = "lasterror";
    public static boolean isStopLock = false;
    public static long lockTime = 600000;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, TaskMonitorService.class);
        startService(intent);
    }


    public static String intToString(int[] arr){
        String str="";
        for (int i=0;i<arr.length-1;i++){
            str+=arr[i]+",";
        }
        return str+arr[arr.length-1];
    }

    public static int[] stringToint(String str){
        if (TextUtils.isEmpty(str))
            return new int[0];
        String[] split = str.split(",");
        int[] arr = new int[split.length];
        for (int i=0;i<split.length;i++){
            arr[i] = Integer.parseInt(split[i]);
        }
        return arr;
    }

    public static String timeToString(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        Date startDate = new Date(time);
        String timeFormat = sdf.format(startDate);
        String returnStr="";
        String[] split = timeFormat.split(":");
        if (Integer.parseInt(split[0])!=0){
            returnStr = split[0]+"分"+split[1]+"秒";
        }else {
            returnStr = split[1]+"秒";
        }
        return returnStr;
    }

    public static boolean getPermission(Context context){
        long ts = System.currentTimeMillis();
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,ts-10000, ts);
        if (queryUsageStats == null  || queryUsageStats.isEmpty()) {
            return false;
        }
        return true;
    }

    public static boolean isServiceRunning(Context context, String serviceName) {
        if (TextUtils.isEmpty(serviceName))
            return false;
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService =
                (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(100);
        for (int i = 0; i < runningService.size(); i++) {
            Log.d("serviceName==",runningService.get(i).service.getClassName().toString());
            if (runningService.get(i).service.getClassName().toString().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

}
