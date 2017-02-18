package com.rl.videocall.callvideo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rl.videocall.config.RtcConfig;
import com.rl.videocall.global.RLApplication;
import com.rl.videocall.listener.OnDoubleTapListener;
import com.rl.videocall.rtcevent.RLRtcEventHandler;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

/**
 * Created by Administrator on 2017/2/15.
 */
public class VideoCallActivity extends Activity implements RLRtcEventHandler.MyEventHandler {
    private static final String TAG = VideoCallActivity.class.getSimpleName();
    private String channelName;
    private TextView tv_channel;
    private RtcEngine rtcEngine;   //核心类对象
    private FrameLayout fl_container;  //视频的容器
    boolean muteFlag; //静音标识
    private Button bt_muteAudio;
    private int rid;
    boolean videoSize; //为true，表示本地视图的为大试图，为false,本地视图为小视图
    //存储本地视频surfaceView 和远程uid对应的surfaceView
    private final HashMap<Integer, SoftReference<SurfaceView>> mUidList = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videocall_activity);
        init_ui();
        receciverIntent();
        RtcConfig.configRTCPre(); //配置下基本rtc
        rtcEngine = RLApplication.getRtcEngine();
        //1,本地视频ui设置
        setLocalVideoUI();
        //2,注册rtc事件的监听
        RLApplication.getRLRtcEventHandler().setMyEventHandlerListener(this);
        //3,一进来就加入频道
        rtcEngine.joinChannel(null, channelName, null, 0);
    }

    /**
     * 初始化ui
     */
    private void init_ui() {
        fl_container = (FrameLayout) findViewById(R.id.FL_container);
        tv_channel = (TextView) findViewById(R.id.tv_channelName);
        bt_muteAudio = (Button) findViewById(R.id.bt_muteAudio);
    }

    /**
     * 上个界面传来的附加数据
     */
    private void receciverIntent() {
        channelName = getIntent().getStringExtra("channelName");
        if (channelName.isEmpty()) {
            throw new RuntimeException("channel name is null");
        }
        tv_channel.setText(channelName); //设置房间名

    }

    /**
     * 绑定本地视屏，视图，开始预览
     */
    private void setLocalVideoUI() {
        SurfaceView surfaceV = RtcEngine.CreateRendererView(getApplicationContext());

        mUidList.put(0, new SoftReference<SurfaceView>(surfaceV));

        // 1,绑定本地视图，需要手动清除，uid是本地uid
        rtcEngine.setupLocalVideo(new VideoCanvas(surfaceV, VideoCanvas.RENDER_MODE_HIDDEN, 0));
        //???uid
        surfaceV.setZOrderOnTop(false);
        // surfaceV.setZOrderMediaOverlay(false);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams
                .MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        fl_container.addView(surfaceV, params);
        // fl_container.addView(surfaceV);
        // 2,开启预览，离开频道，需要手动停止
        rtcEngine.startPreview();

    }

    /**
     * 移除本地视频的ui
     */
    private void removeLocalVideoUI() {
        rtcEngine.setupLocalVideo(null);
        rtcEngine.stopPreview();
    }


    /**
     * 点击加入频道
     *
     * @param v
     */
    public void joinChannel(View v) {
        Log.i(TAG, "点击了");
        rtcEngine.joinChannel(null, channelName, null, 0);
    }

    /**
     * 用户点击挂断
     *
     * @param v
     */
    public void leaveChannel(View v) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeLocalVideoUI();
        rtcEngine.leaveChannel(); //离开频道
        RLApplication.getRLRtcEventHandler().removeMyEventHandlerListener(); //移除对rtc事件的监听
    }


    /**
     * 设置远程用户的ui
     *
     * @param uid
     */
    @Override
    public void onFirstRemoteVideoDecoded(int uid) {
        setRemoteVideoUI(uid); //绑定远程用户的ui
    }

    /**
     * 有用户离开频道
     *
     * @param uid
     * @param reason
     */
    @Override
    public void onUserOffline(int uid, int reason) {
        fl_container.removeViewAt(1);  //移除远程视图
    }

    /**
     * 设置远程端的视频的UI
     *
     * @param uid 远程用户的id
     */
    private void setRemoteVideoUI(final int uid) {
        rid = uid;
        //回掉不能做耗时的操作，所以需要在其他线程运行
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                afreshLocalVideoUI();

                final SurfaceView surfaceView = RtcEngine.CreateRendererView
                        (getApplicationContext()); //创建渲染试图
                mUidList.put(uid, new SoftReference<SurfaceView>(surfaceView));

                // surfaceView.setZOrderMediaOverlay(false);
                surfaceView.setZOrderOnTop(false);
                surfaceView.setZOrderMediaOverlay(false);
                Log.i("server", "live 的uid" + uid);
                rtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, Constants
                        .RENDER_MODE_HIDDEN, uid));
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup
                        .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                fl_container.addView(surfaceView, params);

                surfaceView.setEnabled(false); //设置不可点击
                surfaceView.setOnTouchListener(new OnDoubleTapListener(getApplicationContext()) {
                    @Override
                    public void onDoubleTap(View view, MotionEvent e) {
                        Log.i(TAG, "远程试图双击事件" + "videoSize=" + videoSize);

                        fl_container.removeAllViews();
                        SurfaceView surfaceLocall = mUidList.get(0).get();
                        surfaceLocall.setZOrderOnTop(true); //放到顶层
                        //本地视图的设置
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(400, 300);
                        params.gravity = Gravity.END;
                        view.setLayoutParams(params);
                        fl_container.addView(surfaceLocall, params);

                        //远程视图的设置
                        FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(ViewGroup
                                .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        surfaceView.setZOrderMediaOverlay(false);
                        fl_container.addView(surfaceView, params1);
                        surfaceLocall.setEnabled(true);
                        surfaceView.setEnabled(false);
                    }
                });


                //T通话用小流
                // rtcEngine.setRemoteVideoStreamType(uid,Constants.VIDEO_STREAM_MEDIUM);
            }
        });
    }

    /**
     * 静音
     *
     * @param v
     */
    public void muteAudio(View v) {
        if (muteFlag) {
            muteFlag = false;
            rtcEngine.muteLocalAudioStream(false);
            bt_muteAudio.setBackgroundColor(getResources().getColor(R.color.purple));  //设置紫色
        } else {
            //静音
            muteFlag = true;
            rtcEngine.muteLocalAudioStream(true);
            bt_muteAudio.setBackgroundColor(Color.GRAY);
        }
    }

    /**
     * 当和远程用户链接，重新设置本地视频ui
     */
    private void afreshLocalVideoUI() {
        //1,父容器清除子类
        fl_container.removeAllViews();

        final SurfaceView surfaceV = mUidList.get(0).get(); //获取本地视图的surfaceView
        // 1,绑定本地视图，需要手动清除，uid是本地uid
        rtcEngine.setupLocalVideo(new VideoCanvas(surfaceV, VideoCanvas.RENDER_MODE_HIDDEN, 0));
        //???uid
        surfaceV.setZOrderOnTop(true);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(400, 300);
        params.gravity = Gravity.END;
        fl_container.addView(surfaceV, params);
        //本地视屏注册触摸事件,双击
        surfaceV.setOnTouchListener(new OnDoubleTapListener(getApplicationContext()) {
            @Override
            public void onDoubleTap(View view, MotionEvent e) {//改变布局的大小
                SurfaceView surfaceRemote = mUidList.get(rid).get();
                Log.i(TAG, "本地试图双击事件" + "videoSize=" + videoSize);
                fl_container.removeAllViews();
                surfaceV.setZOrderOnTop(false); //放到底层
                //本地视图的设置
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup
                        .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                view.setLayoutParams(params);
                fl_container.addView(surfaceV, params);

                //远程视图的设置

                FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(400, 300);
                params1.gravity = Gravity.END;
                surfaceRemote.setZOrderMediaOverlay(true);
                fl_container.addView(surfaceRemote, params1);
                surfaceV.setEnabled(false);
                surfaceRemote.setEnabled(true);
            }
        });
        // 2,开启预览，离开频道，需要手动停止
        rtcEngine.startPreview();
    }
}
