package com.org.tk.softlock.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.org.tk.softlock.LockApplication;
import com.org.tk.softlock.databinding.ActivityInputNumPswLockBinding;

public class InputNumPswLockActivity extends AppCompatActivity {

    private Context context;
    private ActivityInputNumPswLockBinding lockBinding;
    private String packageName="";
    private PackageManager packageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lockBinding = ActivityInputNumPswLockBinding.inflate(getLayoutInflater());
        setContentView(lockBinding.getRoot());
        context = this;

        packageName = getIntent().getStringExtra("packageName");
        packageManager = context.getPackageManager();

        initData();
    }

    private void initData() {
        lockBinding.tvSoftName.setText(getApplicationLabel(context,packageName));
        lockBinding.tvUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LockApplication.isStopLock = true;
                if (!TextUtils.isEmpty(packageName)) {
                    Intent intent = packageManager.getLaunchIntentForPackage(packageName);
                    startActivity(intent);
                }
                InputNumPswLockActivity.this.finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addCategory("android.intent.category.MONKEY");
        startActivity(intent);
    }

    public CharSequence getApplicationLabel(Context context, String pkgName){
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, PackageManager.GET_META_DATA);
            return pm.getApplicationLabel(appInfo);
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "软件";
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
