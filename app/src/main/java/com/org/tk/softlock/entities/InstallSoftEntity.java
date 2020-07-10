package com.org.tk.softlock.entities;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

public class InstallSoftEntity {
    ApplicationInfo info;
    boolean isCheck;

    public InstallSoftEntity(ApplicationInfo info, boolean isCheck) {
        this.info = info;
        this.isCheck = isCheck;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public ApplicationInfo getInfo() {
        return info;
    }

    public void setInfo(ApplicationInfo info) {
        this.info = info;
    }
}
