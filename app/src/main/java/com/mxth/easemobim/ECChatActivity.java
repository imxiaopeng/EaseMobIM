package com.mxth.easemobim;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.ui.EaseChatFragment;

/**
 * Created by Administrator on 2017/5/18.
 */

public class ECChatActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecchat);
        String toChatUsername = getIntent().getExtras().getString(EaseConstant.EXTRA_USER_ID);
        EaseChatFragment  chatFragment = new EaseChatFragment();
        //set arguments
        Bundle b = new Bundle();
        b.putString(EaseConstant.EXTRA_USER_ID,toChatUsername);
        b.putInt(EaseConstant.EXTRA_CHAT_TYPE,EaseConstant.CHATTYPE_SINGLE);
        chatFragment.setArguments(b);
        getSupportFragmentManager().beginTransaction().add(R.id.fl_content, chatFragment).commit();
    }
}
