package com.org.tk.softlock.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.org.tk.softlock.LockApplication;
import com.org.tk.softlock.R;
import com.org.tk.softlock.databinding.ActivitySetSoftPassWordBinding;
import com.org.tk.softlock.gesture.GestureLockViewGroup;

import java.util.ArrayList;
import java.util.List;

import static com.org.tk.softlock.LockApplication.intToString;
import static com.org.tk.softlock.LockApplication.stringToint;

public class SetSoftPassWordActivity extends AppCompatActivity {

    ActivitySetSoftPassWordBinding softPassWordBinding;
    private SharedPreferences preferences;
    private String passwordStr="";
    private int[] savePass;
    /**
     * 保持用户选中的GestureLockView的ID
     */
    private List<Integer> mChoose = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        softPassWordBinding = ActivitySetSoftPassWordBinding.inflate(getLayoutInflater());
        setContentView(softPassWordBinding.getRoot());

        initData();
    }

    private void initData() {
        passwordStr="";
        softPassWordBinding.lockView.setmAnswer(new int[0]);
        preferences = getSharedPreferences(LockApplication.softPre,MODE_PRIVATE);
        passwordStr = preferences.getString(LockApplication.softpassword, "");
        Log.e("password","savePassword"+passwordStr);

        if (TextUtils.isEmpty(passwordStr)){
            softPassWordBinding.llSet.setVisibility(View.VISIBLE);
            softPassWordBinding.llInput.setVisibility(View.GONE);
            softPassWordBinding.rlForget.setVisibility(View.GONE);
        }else {
            softPassWordBinding.llInput.setVisibility(View.VISIBLE);
            softPassWordBinding.llSet.setVisibility(View.GONE);
            savePass = stringToint(passwordStr);
            softPassWordBinding.lockView.setmAnswer(savePass);
            softPassWordBinding.lockView.setmTryTimes(5);
            softPassWordBinding.rlForget.setVisibility(View.VISIBLE);
        }
        softPassWordBinding.lockView.setOnGestureLockViewListener(new GestureLockViewGroup.OnGestureLockViewListener() {
            @Override
            public void onBlockSelected(int cId) {
                mChoose.add(cId);
            }

            @Override
            public void onGestureEvent(boolean matched, int[] password) {
                if (!TextUtils.isEmpty(passwordStr)){
                    if (matched){
                        softPassWordBinding.llSet.setVisibility(View.VISIBLE);
                        softPassWordBinding.lockView.setmAnswer(new int[0]);
                        passwordStr = "";
                        softPassWordBinding.llInput.setVisibility(View.VISIBLE);
                        softPassWordBinding.llInput.setText("请输入新密码");
                        softPassWordBinding.rlForget.setVisibility(View.GONE);
                    }
                }else {
                    int[] pass = new int[mChoose.size()];
                    for (int i=0;i<mChoose.size();i++){
                        pass[i] = mChoose.get(i);
                    }
                    if (password == null || password.length == 0) {
                        softPassWordBinding.lockView.setmAnswer(pass);
                        softPassWordBinding.llInput.setVisibility(View.VISIBLE);
                        softPassWordBinding.llInput.setText("请再次确认密码");
                    } else {
                        if (matched) {
                            passwordStr = intToString(password);
//                            preferences.edit().putString(LockApplication.softpassword, intToString(password)).apply();
                            Toast.makeText(SetSoftPassWordActivity.this, "请设置问题", Toast.LENGTH_SHORT).show();
                            softPassWordBinding.llUp.setVisibility(View.GONE);
                            softPassWordBinding.llBottom.setVisibility(View.VISIBLE);
                        }
                    }
                }
                mChoose.clear();
            }

            @Override
            public void onUnmatchedExceedBoundary() {
                long lastTime = preferences.getLong(LockApplication.lastTime,System.currentTimeMillis());
                if (System.currentTimeMillis()-lastTime>LockApplication.lockTime){
                    preferences.edit().putBoolean(LockApplication.lastError,false);
                    preferences.edit().putLong(LockApplication.lastTime,System.currentTimeMillis());
                }
                Toast.makeText(SetSoftPassWordActivity.this,"输入次数过多，请"+LockApplication.timeToString(LockApplication.lockTime-(System.currentTimeMillis()-lastTime))+"后再试",Toast.LENGTH_SHORT).show();
            }
        });

        softPassWordBinding.etAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(softPassWordBinding.etAnswer.getText().toString())){
                    softPassWordBinding.tvCommit.setEnabled(true);
                    softPassWordBinding.tvCommit.setTextColor(getResources().getColor(R.color.text_top_cancel));
                }else {
                    softPassWordBinding.tvCommit.setEnabled(false);
                    softPassWordBinding.tvCommit.setTextColor(getResources().getColor(R.color.gray));
                }
            }
        });
        softPassWordBinding.tvCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.edit().putString(LockApplication.softpassword, passwordStr).apply();
                preferences.edit().putString(LockApplication.softquestion, softPassWordBinding.etAnswer.getText().toString().trim()).apply();
                SetSoftPassWordActivity.this.finish();
            }
        });
        softPassWordBinding.tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetSoftPassWordActivity.this.finish();
            }
        });
        softPassWordBinding.tvForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SetSoftPassWordActivity.this,ClearPassWordActivity.class).putExtra("from","set"));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }
}
