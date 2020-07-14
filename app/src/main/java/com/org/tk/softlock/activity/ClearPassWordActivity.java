package com.org.tk.softlock.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.org.tk.softlock.LockApplication;
import com.org.tk.softlock.databinding.ActivityClearPassWordBinding;

public class ClearPassWordActivity extends AppCompatActivity {

    private ActivityClearPassWordBinding clearPassWordActivity;
    private SharedPreferences preferences;
    private String answer;
    private String from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clearPassWordActivity = ActivityClearPassWordBinding.inflate(getLayoutInflater());
        setContentView(clearPassWordActivity.getRoot());
//.putExtra("from","input")
        from = getIntent().getStringExtra("from");
        preferences = getSharedPreferences(LockApplication.softPre,MODE_PRIVATE);
        answer = preferences.getString(LockApplication.softquestion, "");
        clearPassWordActivity.etAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(clearPassWordActivity.etAnswer.getText().toString())){
                    clearPassWordActivity.btCommit.setEnabled(true);
                }else {
                    clearPassWordActivity.btCommit.setEnabled(false);
                }
            }
        });
        clearPassWordActivity.btCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clearPassWordActivity.etAnswer.getText().toString().trim().equals(answer)){
                    preferences.edit().putString(LockApplication.softpassword, "").apply();
                    preferences.edit().putString(LockApplication.softquestion, "").apply();
                    ClearPassWordActivity.this.finish();
                }else {
                    Toast.makeText(ClearPassWordActivity.this,"您输入的答案不对噢~",Toast.LENGTH_SHORT).show();
                }
            }
        });
        clearPassWordActivity.tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });
    }

    private void goBack(){
        if (from.equals("input")){
            Intent intent = new Intent();
            intent.setAction("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.HOME");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addCategory("android.intent.category.MONKEY");
            startActivity(intent);
            ClearPassWordActivity.this.finish();
        }else {
            ClearPassWordActivity.this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        goBack();
    }
}
