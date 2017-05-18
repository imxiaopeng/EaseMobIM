package com.mxth.easemobim;

import android.util.Log;

/**
 * Created by Administrator on 2017/1/23.
 */

public class LogUtil {
    private final static String TAG="日志：MESSAGE";
    public  static void e(Object o){
        Log.e(TAG,o+"");
    }
}
