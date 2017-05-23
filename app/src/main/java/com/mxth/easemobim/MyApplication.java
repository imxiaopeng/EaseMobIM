package com.mxth.easemobim;

import android.app.Application;
import android.content.Context;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.easeui.EaseUI;
import com.hyphenate.easeui.domain.EaseUser;

/**
 * Created by Administrator on 2017/1/18.
 */

public class MyApplication extends Application {
    public static Context ctx;
    @Override
    public void onCreate() {
        super.onCreate();
        ctx=getApplicationContext();
        EMOptions options = new EMOptions();
        options.setAutoLogin(true);
// 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(false);
        EaseUI.getInstance().init(this,options);
//        EaseUI.getInstance().
//初始化
        EMClient.getInstance().init(ctx, options);
//在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(true);
    }
}

