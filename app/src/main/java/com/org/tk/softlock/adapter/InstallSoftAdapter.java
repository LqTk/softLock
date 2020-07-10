package com.org.tk.softlock.adapter;

import android.content.pm.PackageInfo;
import android.widget.CheckBox;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.org.tk.softlock.R;
import com.org.tk.softlock.entities.InstallSoftEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InstallSoftAdapter extends BaseQuickAdapter<InstallSoftEntity, BaseViewHolder> {
    public InstallSoftAdapter(int layoutResId, @Nullable List<InstallSoftEntity> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder viewHolder, InstallSoftEntity info) {
        viewHolder.setText(R.id.tv_soft_name,info.getInfo().loadLabel(getContext().getPackageManager()).toString());
        viewHolder.setImageDrawable(R.id.iv_soft_icon,info.getInfo().loadIcon(getContext().getPackageManager()));
        CheckBox checkBox = viewHolder.getView(R.id.cb_choose);
        checkBox.setChecked(info.isCheck());
    }
}
