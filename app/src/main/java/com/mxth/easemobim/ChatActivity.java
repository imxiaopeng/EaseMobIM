package com.mxth.easemobim;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/1/24.
 */

public class ChatActivity extends AppCompatActivity implements View.OnFocusChangeListener {
    @Bind(R.id.tool)
    Toolbar tool;
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    @Bind(R.id.et_msg)
    EditText etMsg;
    private String userName;
    private EMMessageListener msgListener;
    private MyAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
//        InputMethodManager manager= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        manager.set
        tool.setTitleTextColor(Color.WHITE);
        userName = getIntent().getStringExtra("userName");
        LogUtil.e(userName);
        adapter = new MyAdapter();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                recyclerView.smoothScrollToPosition(adapter.beans.size() - 1);
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        tool.setTitle(TextUtils.isEmpty(userName) ? "null" : userName);
        tool.setNavigationIcon(R.mipmap.back_);
        setSupportActionBar(tool);
        tool.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if ((event.getAction() == MotionEvent.ACTION_DOWN)
                        && (view.getId() == R.id.recyclerView)) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    etMsg.clearFocus();
                }
                return false;
            }
        });
        msgListener = new EMMessageListener() {

            @Override
            public void onMessageReceived(List<EMMessage> messages) {
                //收到消息
                LogUtil.e("收到消息onMessageReceived");
                for (EMMessage m : messages) {
                    LogUtil.e(m.toString());
                }
                EMMessage emMessage = messages.get(0);
                if (emMessage.getBody() instanceof EMTextMessageBody) {
                    EMTextMessageBody body = (EMTextMessageBody) emMessage.getBody();
                    final MessageBean bean = new MessageBean(body.getMessage(), MessageBean.TYPE_OTHER);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.addItem(bean);
                        }
                    });
                }
            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {
                //收到透传消息
                LogUtil.e("收到透传消息");
                for (EMMessage m : messages) {
                    LogUtil.e(m.toString());
                }
            }

            @Override
            public void onMessageRead(List<EMMessage> list) {
                //收到已读回执
                LogUtil.e("收到已读回执");
                for (EMMessage m : list) {
                    LogUtil.e(m.toString());
                }
            }

            @Override
            public void onMessageDelivered(List<EMMessage> list) {
                //收到已送达回执
                LogUtil.e("收到消息onMessageDelivered");
                for (EMMessage m : list) {
                    LogUtil.e(m.toString());
                }
            }

            @Override
            public void onMessageChanged(EMMessage message, Object change) {
                //消息状态变动
                LogUtil.e("收到透传消息");
                LogUtil.e(message + ",change:" + change);
            }
        };
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
        etMsg.setOnFocusChangeListener(this);
    }

    public void send(View v) {
        final String msg = etMsg.getText().toString().trim();
        if (TextUtils.isEmpty(msg)) {
            ToastUtil.show("不能发送空消息!");
            return;
        }
        if (TextUtils.isEmpty(userName)) {
            ToastUtil.show("当前用户获取错误!");
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //创建一条文本消息，content为消息文字内容，toChatUsername为对方用户或者群聊的id，后文皆是如此
                EMMessage message = EMMessage.createTxtSendMessage(msg, userName);
                //发送消息
                EMClient.getInstance().chatManager().sendMessage(message);
                LogUtil.e(message.toString());
            }
        });
        etMsg.setText("");
        adapter.addItem(new MessageBean(msg, MessageBean.TYPE_SELF));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //        记得在不需要的时候移除listener，如在activity的onDestroy()时
        EMClient.getInstance().chatManager().removeMessageListener(msgListener);
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();
        if (b && isOpen) {
            if (adapter.beans.size() > 0) {
                recyclerView.smoothScrollToPosition(adapter.beans.size() - 1);
            }
        }
    }


    private class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private ArrayList<MessageBean> beans = new ArrayList<>();

        public MyAdapter() {
        }

        public void addItem(MessageBean bean) {
            beans.add(bean);
            notifyDataSetChanged();

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewHolderSelf holder_self = null;
            ViewHolderOther holder_other = null;
            if (viewType == MessageBean.TYPE_SELF) {
                View view = View.inflate(ChatActivity.this, R.layout.item_chat_right, null);
                holder_self = new ViewHolderSelf(view);
                return holder_self;
            } else {
                View view = View.inflate(ChatActivity.this, R.layout.item_chat_left, null);
                holder_other = new ViewHolderOther(view);
                return holder_other;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MessageBean bean = beans.get(position);
            ViewHolderSelf holder_self = null;
            ViewHolderOther holder_other = null;
            if (bean.msgType == MessageBean.TYPE_SELF) {
                holder_self = (ViewHolderSelf) holder;
                holder_self.tv_msg.setText(bean.content);
            } else if (bean.msgType == MessageBean.TYPE_OTHER) {
                holder_other = (ViewHolderOther) holder;
                holder_other.tv_msg.setText(bean.content);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return beans.get(position).msgType;
        }

        @Override
        public int getItemCount() {
            return beans.size();
        }
    }

    private class ViewHolderSelf extends RecyclerView.ViewHolder {

        private final TextView tv_msg;

        public ViewHolderSelf(View itemView) {
            super(itemView);
            tv_msg = (TextView) itemView.findViewById(R.id.tv_msg);
        }
    }

    private class ViewHolderOther extends RecyclerView.ViewHolder {

        private final TextView tv_msg;

        public ViewHolderOther(View itemView) {
            super(itemView);
            tv_msg = (TextView) itemView.findViewById(R.id.tv_msg);
        }
    }

}
