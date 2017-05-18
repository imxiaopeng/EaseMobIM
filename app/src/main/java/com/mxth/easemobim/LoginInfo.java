package com.mxth.easemobim;

/**
 * Created by Administrator on 2017/1/18.
 */

public class LoginInfo {
    public String account;
    public String pwd;
    public int flag;

    public LoginInfo(int flag,String account, String pwd) {
        this.account = account;
        this.pwd = pwd;
        this.flag = flag;
    }
}
