package com.org.tk.softlock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.org.tk.softlock.service.TaskMonitorService;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    /** 系统默认的UncaughtException处理类 **/
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private Context context;

    public static Thread.UncaughtExceptionHandler getAppExceptionHandler(Context context) {
        return new CrashHandler(context);
    }

    public CrashHandler(Context context) {
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
        this.context = context;
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        }else {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            // 重新启动应用
            Intent intent = new Intent(context, TaskMonitorService.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent restartIntent =
                    PendingIntent.getActivity(context, 0, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用
            // 退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    /**
     * @return true:如果处理了该异常信息;否则返回false.
     * @throws
     * @描述:自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     */
    private boolean handleException(Throwable ex) {
        if (null == ex) {
            return false;
        }
        return true;
    }
}
