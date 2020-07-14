package com.org.tk.softlock.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.org.tk.softlock.CrashHandler;
import com.org.tk.softlock.LockApplication;
import com.org.tk.softlock.R;
import com.org.tk.softlock.adapter.AddSoftAdapter;
import com.org.tk.softlock.databinding.ActivityMainBinding;
import com.org.tk.softlock.event.CheckSoftChange;
import com.org.tk.softlock.service.TaskMonitorService;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mainBinding;
    private SharedPreferences preferences;
    private String hasSoftName;
    private AddSoftAdapter softAdapter;
    private List<ApplicationInfo> packageInfoList = new ArrayList<>();
    private List<String> softName = new ArrayList<>();
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        context = MainActivity.this;
        EventBus.getDefault().register(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission();
        }

        preferences = getSharedPreferences(LockApplication.softPre,MODE_PRIVATE);
        hasSoftName = preferences.getString(LockApplication.softname, "");
        initView();
        ClickListener();
        refreshData(new CheckSoftChange());
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler.getAppExceptionHandler(MainActivity.this));
    }

    private void initView() {
        showWaring();
        if (!LockApplication.getPermission(MainActivity.this)){
            Intent intent = new Intent( Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mainBinding.recyclerSoft.setLayoutManager(layoutManager);
        softAdapter = new AddSoftAdapter(R.layout.soft_item_add,packageInfoList);
        mainBinding.recyclerSoft.setAdapter(softAdapter);

        softAdapter.addChildClickViewIds(R.id.tv_delete);
        softAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                switch (view.getId()){
                    case R.id.tv_delete:
                        ApplicationInfo item = (ApplicationInfo) adapter.getItem(position);
                        if (softName.contains(item.packageName)){
                            softName.remove(item.packageName);
                            preferences.edit().putString(LockApplication.softname,softName.toString().substring(1,softName.toString().length()-1)).apply();
                            packageInfoList.remove(item);
                            softAdapter.notifyDataSetChanged();
                        }
                        if (packageInfoList!=null && packageInfoList.size()==0){
                            mainBinding.rlNosoft.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }
        });
    }

    private void showWaring() {
        String passwordStr = preferences.getString(LockApplication.softpassword, "");
        if (TextUtils.isEmpty(passwordStr)){
            mainBinding.tvWaring.setVisibility(View.VISIBLE);
        }else {
            mainBinding.tvWaring.setVisibility(View.GONE);
        }
    }

    private void ClickListener() {
        mainBinding.ivAddSoft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, InstallSoftActivity.class));
            }
        });
        mainBinding.tvSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,SetSoftPassWordActivity.class);
                startActivity(intent);
            }
        });
        mainBinding.llAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, InstallSoftActivity.class));
            }
        });
    }

    @Subscribe
    public void refreshData(CheckSoftChange softChange){
        hasSoftName = preferences.getString(LockApplication.softname, "");
        packageInfoList.clear();
        softName.clear();
        String[] hasName = hasSoftName.split(",");
        for (int i=0;i<hasName.length;i++){
            if (!TextUtils.isEmpty(hasName[i])) {
                softName.add(hasName[i].replaceAll(" ",""));
            }
        }
        PackageManager pm = this.getPackageManager();
        // 查询所有已经安装的应用程序
        List<ApplicationInfo> listAppcations = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        Collections.sort(listAppcations,new ApplicationInfo.DisplayNameComparator(pm));// 排序
        // 第三方应用程序
        for (ApplicationInfo app : listAppcations) {
            //非系统程序
            if (softName.contains(app.packageName)) {
                packageInfoList.add(app);
            }
        }
        if (packageInfoList!=null && packageInfoList.size()==0){
            mainBinding.rlNosoft.setVisibility(View.VISIBLE);
        }else {
            mainBinding.rlNosoft.setVisibility(View.GONE);
        }
        softAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        //保活
        Intent intent = new Intent(context, TaskMonitorService.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent restartIntent =
                PendingIntent.getActivity(context, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData(new CheckSoftChange());
        showWaring();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.PACKAGE_USAGE_STATS)
                .subscribe(new io.reactivex.Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {

                        } else {

                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }
}
