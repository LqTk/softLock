package com.org.tk.softlock.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.org.tk.softlock.LockApplication;
import com.org.tk.softlock.databinding.ActivityInputNumPswLockBinding;
import com.org.tk.softlock.gesture.GestureLockViewGroup;

import static com.org.tk.softlock.LockApplication.stringToint;

public class InputNumPswLockActivity extends AppCompatActivity {

    private Context context;
    private ActivityInputNumPswLockBinding lockBinding;
    private String packageName="";
    private PackageManager packageManager;
    private int[] password;
    private String passwordStr="";
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lockBinding = ActivityInputNumPswLockBinding.inflate(getLayoutInflater());
        setContentView(lockBinding.getRoot());
        context = this;

        packageName = getIntent().getStringExtra("packageName");
        packageManager = context.getPackageManager();
        preferences = getSharedPreferences(LockApplication.softPre,MODE_PRIVATE);
        passwordStr = preferences.getString(LockApplication.softpassword, "");

        initData();
    }

    private void initData() {
        password = stringToint(passwordStr);
        lockBinding.tvSoftName.setText(getApplicationLabel(context,packageName));
        lockBinding.lockView.setmAnswer(password);
        lockBinding.lockView.setmTryTimes(5);
        lockBinding.lockView.setOnGestureLockViewListener(new GestureLockViewGroup.OnGestureLockViewListener() {
            @Override
            public void onBlockSelected(int cId) {
            }

            @Override
            public void onGestureEvent(boolean matched, int[] password) {
                if (matched){
                    LockApplication.isStopLock = true;
                    InputNumPswLockActivity.this.finish();
                }
            }

            @Override
            public void onUnmatchedExceedBoundary() {
                long lastTime = preferences.getLong(LockApplication.lastTime,System.currentTimeMillis());
                if (System.currentTimeMillis()-lastTime>LockApplication.lockTime){
                    preferences.edit().putBoolean(LockApplication.lastError,false);
                    preferences.edit().putLong(LockApplication.lastTime,System.currentTimeMillis());
                }
                Toast.makeText(InputNumPswLockActivity.this,"输入次数过多，请"+LockApplication.timeToString(LockApplication.lockTime-(System.currentTimeMillis()-lastTime))+"后再试",Toast.LENGTH_SHORT).show();

            }
        });
        lockBinding.tvForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(InputNumPswLockActivity.this,ClearPassWordActivity.class).putExtra("from","input"));
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
