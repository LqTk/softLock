package com.org.tk.softlock.adapter;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.org.tk.softlock.R;
import com.org.tk.softlock.entities.InstallSoftEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AddSoftAdapter extends BaseQuickAdapter<ApplicationInfo, BaseViewHolder> {
    public AddSoftAdapter(int layoutResId, @Nullable List<ApplicationInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder viewHolder, ApplicationInfo info) {
        viewHolder.setText(R.id.tv_soft_name,info.loadLabel(getContext().getPackageManager()).toString());
        viewHolder.setImageDrawable(R.id.iv_soft_icon,info.loadIcon(getContext().getPackageManager()));
    }
}
