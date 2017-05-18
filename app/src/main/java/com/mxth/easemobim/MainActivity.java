package com.mxth.easemobim;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMContactManager;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.exceptions.HyphenateException;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import static com.mxth.easemobim.R.id.ll_newMsg;


public class MainActivity extends AppCompatActivity {
    public static int LOGOUT = 2;
    private ListView lv_friends;
    private EMContactManager manager;
    private MyAdapter adapter;
    private TextView tv_msg;
    private List<String> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        tv_msg = (TextView) findViewById(R.id.tv_msg);
        manager = EMClient.getInstance().contactManager();
        lv_friends = (ListView) findViewById(R.id.lv_friends);
        adapter = new MyAdapter();
        lv_friends.setAdapter(adapter);
//        initEvent();
        manager.aysncGetAllContactsFromServer(new EMValueCallBack<List<String>>() {
            @Override
            public void onSuccess(final List<String> strings) {
                users = strings;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (strings != null && strings.size() == 0) {
                            Toast.makeText(MainActivity.this, "没有好友！", Toast.LENGTH_SHORT).show();
                        } else {
                            adapter.flush(strings);
                        }
                    }
                });
            }

            @Override
            public void onError(int i, String s) {
                LogUtil.e("异常:" + s + ",code=" + i);
            }
        });
        manager.setContactListener(new EMContactListener() {
            @Override
            public void onContactInvited(final String username, final String reason) {
                //收到好友邀请
                LogUtil.e("收到" + username + "的好友申请:" + reason);
                LogUtil.e(Thread.currentThread().getId());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
                        adb.setTitle("收到好友申请");
                        adb.setMessage("来自" +username+"的申请:" + reason);
                        adb.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    manager.declineInvitation(username);
                                } catch (HyphenateException e) {
                                    e.printStackTrace();
                                    LogUtil.e("异常:" + e.getErrorCode() + ",," + e.getMessage());
                                }
                                dialogInterface.dismiss();
                            }
                        });
                        adb.setPositiveButton("同意", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    manager.acceptInvitation(username);
                                } catch (HyphenateException e) {
                                    e.printStackTrace();
                                    LogUtil.e("异常:" + e.getErrorCode() + ",," + e.getMessage());
                                }
                                dialogInterface.dismiss();
                            }
                        });
                        AlertDialog dialog = adb.create();
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                    }
                });
            }

            @Override
            public void onFriendRequestAccepted(String s) {
                //好友请求被同意
                LogUtil.e("同意申请:" + s);
            }

            @Override
            public void onFriendRequestDeclined(String s) {
                //好友请求被拒绝
                LogUtil.e("拒绝申请:" + s);
            }

            @Override
            public void onContactDeleted(final String username) {
                //被删除时回调此方法
                LogUtil.e("删除好友:" + username);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.remove(username);
                    }
                });
            }


            @Override
            public void onContactAdded(final String username) {
                //增加了联系人时回调此方法
                LogUtil.e("新增好友:" + username);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.addItem(username);
                    }
                });
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_edit) {
                    EMClient.getInstance().logout(true, new EMCallBack() {

                        @Override
                        public void onSuccess() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                    EventBus.getDefault().post(new LoginInfo(MainActivity.LOGOUT, EMClient.getInstance().getCurrentUser(), ""));
                                }
                            });
                        }

                        @Override
                        public void onError(int i, String s) {

                        }

                        @Override
                        public void onProgress(int i, String s) {

                        }
                    });
                } else if (item.getItemId() == R.id.action_add) {
                    View v = View.inflate(MainActivity.this, R.layout.layout_dialog_addfriend, null);
                    final EditText etName = (EditText) v.findViewById(R.id.et_name);
                    final EditText etReason = (EditText) v.findViewById(R.id.et_reason);
                    Button btnCancle = (Button) v.findViewById(R.id.btn_cancle);
                    Button btnSend = (Button) v.findViewById(R.id.btn_send);
                    final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("添加好友")
                            .setView(v)
                            .create();
                    dialog.setCanceledOnTouchOutside(false);
                    btnCancle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    btnSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String name = etName.getText().toString().trim();
                            String reason = etReason.getText().toString().trim();
                            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(reason)) {
                                Toast.makeText(MainActivity.this, "名称或理由不能为空!", Toast.LENGTH_SHORT).show();
                                dialog.show();
                            }
                            if (users != null && users.contains(name)) {
                                Toast.makeText(MainActivity.this, "已经是好友了！", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                //参数为要添加的好友的username和添加理由
                                try {
                                    EMClient.getInstance().contactManager().addContact(name, reason);
                                    dialog.dismiss();
                                } catch (HyphenateException e) {
                                    e.printStackTrace();
                                    if (e.getErrorCode() == 204) {
                                        ToastUtil.show("用户未找到!");
                                        dialog.show();
                                    } else {
                                        dialog.dismiss();
                                    }
                                    LogUtil.e("异常:" + e.getMessage());
                                }
                            }
                        }
                    });
                    dialog.show();
                } else if (item.getItemId() == R.id.action_me) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                    adb.setMessage("当前登录账号:" + EMClient.getInstance().getCurrentUser());
                    adb.show();
                }
                return true;
            }
        });
    }

    private void initEvent() {
        EMMessageListener   msgListener = new EMMessageListener() {

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private class MyAdapter extends BaseAdapter {

        private List<String> usernames = new ArrayList<>();
        public void remove(String user){
            usernames.remove(user);
            notifyDataSetChanged();
        }

        public void flush(List<String> usernames) {
            this.usernames = usernames;
            notifyDataSetChanged();
        }

        public void addItem(String s) {
            usernames.add(s);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return usernames.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (view == null) {
                view = View.inflate(MainActivity.this, R.layout.item_lv_friends, null);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            final String user=usernames.get(i);
            holder.tv_account.setText(user);
            holder.tv_nick.setText(user);
            holder.ll_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(MainActivity.this,ECChatActivity.class);
                    intent.putExtra(EaseConstant.EXTRA_USER_ID,user);
                    startActivity(intent);
                }
            });
            holder.ll_friend.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
                    adb.setTitle("是否删除好友?");
                    adb.setNegativeButton("否",null);
                    adb.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                EMClient.getInstance().contactManager().deleteContact(user);
                            } catch (HyphenateException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    adb.show();
                    return true;
                }
            });
            return view;
        }
    }

    private class ViewHolder {

        private final LinearLayout ll_friend;
        private final TextView tv_nick;
        private final TextView tv_account;
        private final ImageView iv_icon;

        public ViewHolder(View v) {
            ll_friend = (LinearLayout) v.findViewById(R.id.ll_friend);
            iv_icon = (ImageView) v.findViewById(R.id.iv_icon);
            tv_nick = (TextView) v.findViewById(R.id.tv_nick);
            tv_account = (TextView) v.findViewById(R.id.tv_account);
        }
    }

    //    public void addFriend(View view)
    public void accept(View v) {

    }

    public void disAccept(View v) {

    }

    @Override
    public void onBackPressed() {
        new Thread() {
            @Override
            public void run() {
                EMClient.getInstance().logout(true);
            }
        }.start();
        super.onBackPressed();
    }
}
