package com.org.tk.softlock.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.org.tk.softlock.LockApplication;
import com.org.tk.softlock.R;
import com.org.tk.softlock.adapter.AddSoftAdapter;
import com.org.tk.softlock.databinding.ActivityMainBinding;
import com.org.tk.softlock.event.CheckSoftChange;
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
    private List<ApplicationInfo> packageInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        EventBus.getDefault().register(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission();
        }

        preferences = getSharedPreferences(LockApplication.softPre,MODE_PRIVATE);
        hasSoftName = preferences.getString(LockApplication.softname, "");
        packageInfoList = new ArrayList<>();
        initView();
        ClickListener();
        refreshData(new CheckSoftChange());
    }

    private void initView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mainBinding.recyclerSoft.setLayoutManager(layoutManager);
        softAdapter = new AddSoftAdapter(R.layout.soft_item_add,packageInfoList);
        mainBinding.recyclerSoft.setAdapter(softAdapter);
    }

    private void ClickListener() {
        mainBinding.ivAddSoft.setOnClickListener(new View.OnClickListener() {
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
        List<String> name = new ArrayList<>();
        String[] hasName = hasSoftName.split(",");
        for (int i=0;i<hasName.length;i++){
            if (!TextUtils.isEmpty(hasName[i])) {
                name.add(hasName[i].replaceAll(" ",""));
            }
        }
        PackageManager pm = this.getPackageManager();
        // 查询所有已经安装的应用程序
        List<ApplicationInfo> listAppcations = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        Collections.sort(listAppcations,new ApplicationInfo.DisplayNameComparator(pm));// 排序
        // 第三方应用程序
        for (ApplicationInfo app : listAppcations) {
            //非系统程序
            if (name.contains(app.packageName)) {
                packageInfoList.add(app);
            }
        }
        softAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData(new CheckSoftChange());
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
