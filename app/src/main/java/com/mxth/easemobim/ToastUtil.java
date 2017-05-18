package com.mxth.easemobim;

import android.widget.Toast;

/**
 * Created by Administrator on 2017/1/18.
 */

public class ToastUtil {
    private static Toast toast;
    public static void show(String s){
        if(toast==null){
            toast=Toast.makeText(MyApplication.ctx,s,Toast.LENGTH_SHORT);
        }else{
            toast.setText(s);
            toast.show();
        }
    }
}
