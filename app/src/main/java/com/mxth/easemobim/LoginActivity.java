package com.mxth.easemobim;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @Bind(R.id.et_account)
    EditText etAccount;
    @Bind(R.id.et_pwd)
    EditText etPwd;
    @Bind(R.id.button)
    Button button;
    @Bind(R.id.tv_regist)
    TextView tvRegist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        if(EMClient.getInstance().isConnected()){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
//        etAccount.setText("1312377666");
//        etPwd.setText("123456");
    }

    @OnClick(R.id.button)
    public void onClick() {//登录
        String account = etAccount.getText().toString().trim();
        String pwd = etPwd.getText().toString().trim();
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(pwd)) {
            ToastUtil.show("用户名或密码不能为空！");
            return;
        }
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("正在登录，请稍后...");
        pd.show();
        pd.setCancelable(false);
        EMClient.getInstance().login(account, pwd, new EMCallBack() {

            @Override
            public void onSuccess() {
                if (!LoginActivity.this.isFinishing())
                    pd.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                });
            }

            @Override
            public void onError(int i, String s) {
                if (!LoginActivity.this.isFinishing())
                    pd.dismiss();
                Log.e("message", "登录失败：" + s);
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }

    @OnClick(R.id.tv_regist)
    public void onClick_() {
        startActivity(new Intent(this, RegistActivity.class));
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(LoginInfo info) {
        if (info == null) {
            return;
        }
        if (info.flag == RegistActivity.SUCCESS) {
            //注册成功
            etAccount.setText(info.account);
            etPwd.setText(info.pwd);
        } else if (info.flag == MainActivity.LOGOUT) {
            startActivity(new Intent(this, this.getClass()));
        }
    }
}
