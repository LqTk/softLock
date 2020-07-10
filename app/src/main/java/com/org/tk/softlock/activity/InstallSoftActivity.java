package com.org.tk.softlock.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.org.tk.softlock.LockApplication;
import com.org.tk.softlock.R;
import com.org.tk.softlock.adapter.InstallSoftAdapter;
import com.org.tk.softlock.databinding.ActivityInstallSoftBinding;
import com.org.tk.softlock.entities.InstallSoftEntity;
import com.org.tk.softlock.event.CheckSoftChange;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InstallSoftActivity extends AppCompatActivity {

    private ActivityInstallSoftBinding installSoftBinding;
    List<InstallSoftEntity> installSoftEntities = new ArrayList<>();
    private InstallSoftAdapter adapter;
    private SharedPreferences preferences;
    private String hasSoftName;
    private String[] hasName;
    private List<String> name=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        installSoftBinding = ActivityInstallSoftBinding.inflate(getLayoutInflater());
        setContentView(installSoftBinding.getRoot());

        preferences = getSharedPreferences(LockApplication.softPre,MODE_PRIVATE);
        hasSoftName = preferences.getString(LockApplication.softname, "");
        hasName = hasSoftName.split(",");
        for (int i=0;i<hasName.length;i++){
            if (!TextUtils.isEmpty(hasName[i])) {
                name.add(hasName[i].replaceAll(" ",""));
            }
        }
        initView();
        initData();
        setButtonEnable();
    }

    private void initView() {
        adapter = new InstallSoftAdapter(R.layout.soft_item,installSoftEntities);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        installSoftBinding.recyclerAllSoft.setLayoutManager(linearLayoutManager);
        installSoftBinding.recyclerAllSoft.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                boolean isCheck = installSoftEntities.get(position).isCheck();
                installSoftEntities.get(position).setCheck(!isCheck);
                adapter.notifyDataSetChanged();
                String nowPackageName = installSoftEntities.get(position).getInfo().packageName;
                if (installSoftEntities.get(position).isCheck()){
                    name.add(nowPackageName);
                }else {
                    if (name.contains(nowPackageName)){
                        name.remove(nowPackageName);
                    }
                }
                preferences.edit().putString(LockApplication.softname,name.toString().substring(1,name.toString().length()-1)).apply();
                setButtonEnable();
            }
        });
        installSoftBinding.btCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new CheckSoftChange());
                InstallSoftActivity.this.finish();
            }
        });
    }

    private void initData() {
        PackageManager pm = this.getPackageManager();
        // 查询所有已经安装的应用程序
        List<ApplicationInfo> listAppcations = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        Collections.sort(listAppcations,new ApplicationInfo.DisplayNameComparator(pm));// 排序
        // 第三方应用程序
        for (ApplicationInfo app : listAppcations) {
            //非系统程序
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                if (name.contains(app.packageName)) {
                    installSoftEntities.add(new InstallSoftEntity(app, true));
                }else {
                    installSoftEntities.add(new InstallSoftEntity(app, false));
                }
            }
            //本来是系统程序，被用户手动更新后，该系统程序也成为第三方应用程序了
            else if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0){
                if (name.contains(app.packageName)) {
                    installSoftEntities.add(new InstallSoftEntity(app, true));
                }else {
                    installSoftEntities.add(new InstallSoftEntity(app, false));
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setButtonEnable(){
        if (name==null || name.size()==0){
            installSoftBinding.btCommit.setEnabled(false);
        }else {
            installSoftBinding.btCommit.setEnabled(true);
        }
    }

}
