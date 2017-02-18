package com.rl.videocall.callvideo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.rl.videocall.config.RtcConfig;
import com.rl.videocall.tools.ToastUtils;

public class MainActivity extends AppCompatActivity {

    private EditText et_channel;
    private EditText et_psw;
    private String channel;
    private Button bt_join;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init_ui();
    }

    private void init_ui() {
        et_channel = (EditText) findViewById(R.id.et_channel);
        et_psw = (EditText) findViewById(R.id.et_psw);
        bt_join = (Button) findViewById(R.id.bt_join);
        et_channel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //判断内容是否为空
                channel=et_channel.getText().toString();
                if(channel.isEmpty()){
                    ToastUtils.show("频道不能为空");
                }else{
                    bt_join.setEnabled(true);
                }
            }
        });
    }

    /**
     * 点击按钮
     * @param v
     */
    public void join(View v){
        //跳转到通话界面
       // Intent intent = new Intent(this,VideoCallActivity.class);
        Intent intent = new Intent(this,VideoCallTestActivity.class);
        intent.putExtra("channelName",channel);
        startActivity(intent);
    }
}
