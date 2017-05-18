package com.mxth.easemobim;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.w3c.dom.Text;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegistActivity extends AppCompatActivity {

    public static final int SUCCESS = 1;
    @Bind(R.id.et_account)
    EditText etAccount;
    @Bind(R.id.et_pwd)
    EditText etPwd;
    @Bind(R.id.button2)
    Button btn_regist;
//    @Bind(R.id.tv_title)
//    TextView tv_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool);
        toolbar.setTitle("注册");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button2)
    public void onClick() {//注册
       final String account = etAccount.getText().toString().trim();
        final String pwd = etPwd.getText().toString().trim();
        if(TextUtils.isEmpty(account)||TextUtils.isEmpty(pwd)){
            ToastUtil.show("用户名或密码不能为空!");
            return;
        }
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("注册中...");
        pd.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    // call method in SDK
                    EMClient.getInstance().createAccount(account, pwd);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (!RegistActivity.this.isFinishing())
                                pd.dismiss();
                            ToastUtil.show("注册成功!");
                            finish();
                            EventBus.getDefault().post(new LoginInfo(RegistActivity.SUCCESS,account,pwd));
                        }
                    });
                } catch (final HyphenateException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (!RegistActivity.this.isFinishing())
                                pd.dismiss();
                            int errorCode=e.getErrorCode();
                            if(errorCode== EMError.NETWORK_ERROR){
                                ToastUtil.show("网络错误!");
                            }else if(errorCode == EMError.USER_ALREADY_EXIST){
                                ToastUtil.show("该用户已经注册!");
                            }else if(errorCode == EMError.USER_AUTHENTICATION_FAILED){
                                ToastUtil.show("授权失败!");
                            }else if(errorCode == EMError.USER_ILLEGAL_ARGUMENT){
                                ToastUtil.show("非法的用户名!");
                            }else{
                                ToastUtil.show("注册失败!");
                            }
                        }
                    });
                }
            }
        }).start();
    }

}
