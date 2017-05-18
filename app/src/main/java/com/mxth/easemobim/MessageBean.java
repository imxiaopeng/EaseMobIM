package com.mxth.easemobim;

/**
 * Created by Administrator on 2017/1/24.
 */

public class MessageBean {
    public static final int TYPE_SELF=0;
    public static final int TYPE_OTHER=1;
    public String content;
    public int msgType;

    public MessageBean(String content, int msgType) {
        this.content = content;
        this.msgType = msgType;
    }

    public MessageBean() {

    }
}
