package com.rl.videocall.callvideo;

import android.app.Activity;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
 * Created by Administrator on 2017/2/17.
 */
public class VideoCallTestActivity extends Activity implements RLRtcEventHandler.MyEventHandler {
    private static final String TAG = VideoCallTestActivity.class.getSimpleName();
    private String roomName;
    private TextView tv_room;
    private RtcEngine rtcEngine;                    //核心类对象
    private RelativeLayout rl_bigcontainer;         //大视频的容器
    private FrameLayout fl_small1, fl_small2;        //小视频容器

    boolean muteFlag; //静音标识
    private int rid;
    int count;
    //存储本地视频surfaceView 和远程uid对应的surfaceView
    private final HashMap<Integer, SoftReference<SurfaceView>> mUidList = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videocall_test);
        init_ui();
        receciverIntent();
        RtcConfig.configRTCPre(); //配置下基本rtc
        rtcEngine = RLApplication.getRtcEngine();
        //1,本地视频ui设置
        setLocalVideoUI();
        //2,注册rtc事件的监听
        RLApplication.getRLRtcEventHandler().setMyEventHandlerListener(this);
        //3,一进来就加入频道
        rtcEngine.joinChannel(null, roomName, null, 0);
    }

    /**
     * 初始化ui
     */
    private void init_ui() {
        rl_bigcontainer = (RelativeLayout) findViewById(R.id.rl_bigContainer);  //视频大容器
        fl_small1 = (FrameLayout) findViewById(R.id.fl_small1);                 //视频小容器1
        fl_small2 = (FrameLayout) findViewById(R.id.fl_small2);                 //视频小容器2
        tv_room = (TextView) findViewById(R.id.tv_room);
    }

    /**
     * 上个界面传来的附加数据
     */
    private void receciverIntent() {
        roomName = getIntent().getStringExtra("channelName");
        if (roomName.isEmpty()) {
            throw new RuntimeException("channel name is null");
        }
        tv_room.setText(roomName); //设置房间名

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
        surfaceV.setZOrderOnTop(false);  //设置surfaceView在后面

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams
                .MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rl_bigcontainer.addView(surfaceV, params);
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
        rtcEngine.joinChannel(null, roomName, null, 0);
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
     * 有用户离开频道,移除对应的视图
     *
     * @param uid
     * @param reason
     */
    @Override
    public void onUserOffline(int uid, int reason) {
        //移除对应uid的远程视图
        removeRemoteUI(uid);
    }

    /**
     * 移除对应的uid的远程视图
     *
     * @param uid
     */
    private void removeRemoteUI(final int uid) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SurfaceView remoteView = mUidList.remove(uid).get();  //找到对应的surface
                if (remoteView == null) {
                    throw new RuntimeException("remote view is not exist");
                }
                if (fl_small1.getChildAt(0) == remoteView) {
                    fl_small1.removeView(remoteView);
                    //重新设置ui
                }
                if (fl_small2.getChildAt(0) == remoteView) {
                    fl_small2.removeView(remoteView);
                }
                int childNum = rl_bigcontainer.getChildCount();
                for (int i = 0; i < childNum; i++) {
                    if (rl_bigcontainer.getChildAt(i) == remoteView) {
                        rl_bigcontainer.removeView(remoteView); //1,移除大容器的远程视图
                        //重新设置ui
                        if (mUidList.size() == 1) { //当集合只有一个本地试图
                            SurfaceView local = mUidList.get(0).get();
                            fl_small1.removeView(local);  //小容器移除

                            local.setZOrderOnTop(false);
                            rl_bigcontainer.addView(local);
                        } else {
                            for (Integer uid : mUidList.keySet()) { //遍历uid集合
                                if (uid == 0) {//本地视图
                                    SurfaceView local = mUidList.get(uid).get();
                                    local.setZOrderOnTop(true);
                                } else {//远程视图,填充到大容器
                                    SurfaceView remote = mUidList.get(uid).get();
                                    //找到远程视图的父母，把他解放出来
                                    fl_small2.removeView(remote);
                                    remote.setZOrderOnTop(false);
                                    rl_bigcontainer.addView(remote);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 设置远程端的视频的UI
     *
     * @param uid 远程用户的id
     */
    private void setRemoteVideoUI(final int uid) {
        Log.i(TAG, "调用次数：" + (++count));
        rid = uid;
        //回掉不能做耗时的操作，所以需要在其他线程运行
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //1,生成远程视图，绑定远程视图
                //小容器,增加视图
                final SurfaceView remoteView = RtcEngine.CreateRendererView(getApplicationContext
                        ()); //创建渲染试图
                mUidList.put(uid, new SoftReference<SurfaceView>(remoteView));
                Log.i("server", "live 的uid" + uid);
                rtcEngine.setupRemoteVideo(new VideoCanvas(remoteView, Constants
                        .RENDER_MODE_HIDDEN, uid));
                //2,根据集合大小设置试图
                if (mUidList.size() == 2) {//集合为2个的试图
                    //1,移除本地视图
                    SurfaceView localView = mUidList.get(0).get();
                    rl_bigcontainer.removeView(localView);
                    localView.setZOrderOnTop(true);

                    remoteView.setZOrderOnTop(false);
                    Log.i(TAG, "2集合大小：" + mUidList.size());
                    fl_small1.addView(localView);               //小容器增加本地视图
                    rl_bigcontainer.addView(remoteView);        //大容器增加远程视图


                    //设置监听
                    fl_small1.setOnTouchListener(new OnDoubleTapListener(getApplicationContext()){
                        //双击事件
                        @Override
                        public void onDoubleTap(View view, MotionEvent e) {
                            //大小视图交换
                            SurfaceView localView = mUidList.get(0).get();  //本地视图
                            fl_small1.removeView(localView);

                            rl_bigcontainer.removeView(remoteView);

                            remoteView.setZOrderOnTop(true);
                            fl_small1.addView(remoteView);
                            localView.setZOrderOnTop(false);
                            rl_bigcontainer.addView(localView);
                        }
                    });
                } else if (mUidList.size() == 3) { //集合为3个视图设置
                    Log.i(TAG, "3集合大小：" + mUidList.size());
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup
                            .LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    Log.i(TAG, "小容器的2可见型：" + fl_small2.getVisibility());
                    remoteView.setZOrderOnTop(true);
                    fl_small2.addView(remoteView, params); //小容器增加远程视图

                }
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
            ((ImageView) v).setImageResource(R.mipmap.btn_mute);
        } else {
            //静音
            muteFlag = true;
            rtcEngine.muteLocalAudioStream(true);
            ((ImageView) v).setImageResource(R.mipmap.icon_muted);
        }
    }

    /**
     * 当和远程用户链接，重新设置本地视频ui
     */
    private void afreshLocalVideoUI() {

    }
}
