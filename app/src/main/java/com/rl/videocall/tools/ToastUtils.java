package com.rl.videocall.tools;

import android.widget.Toast;

import com.rl.videocall.global.RLApplication;

/**
 * Created by Administrator on 2017/2/8.
 */
public class ToastUtils {
    private static Toast toast;

    public static void show(final String msg) {
        Runnable runnable= new Runnable() {
            @Override
            public void run() {
                if (toast == null) {
                    toast = Toast.makeText(RLApplication.getGloableContext(), msg, Toast
                            .LENGTH_SHORT);
                } else {
                    toast.setText(msg);
                }
                toast.show();
            }

        };
        RLApplication.getMainHandler().post(runnable);
    }
}
