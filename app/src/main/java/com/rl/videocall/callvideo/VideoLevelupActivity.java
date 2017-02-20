package com.rl.videocall.callvideo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rl.videocall.appcontorl.ViewControl;
import com.rl.videocall.appcontorl.bean.SurfaceModel;
import com.rl.videocall.config.RtcConfig;
import com.rl.videocall.global.RLApplication;
import com.rl.videocall.listener.OnDoubleTapListener;
import com.rl.videocall.rtcevent.RLRtcEventHandler;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

/**
 * Created by Administrator on 2017/2/20.
 */
public class VideoLevelupActivity extends Activity implements RLRtcEventHandler.MyEventHandler{
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

    private List<SurfaceModel> dList = new ArrayList<>();
    private List<ViewGroup> vList = new ArrayList<>();
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

        //容器集合赋值
        vList.add(rl_bigcontainer);
        vList.add(fl_small1);
        vList.add(fl_small2);

        //1,初始化布局控制
        ViewControl.createViewControl(vList);

        //2,注册监听
        ViewControl.getInstance().setDoubleClickListener();
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
        // 1,绑定本地视图，需要手动清除，uid是本地uid
        rtcEngine.setupLocalVideo(new VideoCanvas(surfaceV, VideoCanvas.RENDER_MODE_HIDDEN, 0));
        SurfaceModel surfaceModel = new SurfaceModel(null,0,surfaceV,1,0);
        ViewControl.getInstance().addSurfaceView(surfaceModel);

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
                ViewControl.getInstance().deleteSurfaceView(uid);
            }
        });
    }

    /**
     * 设置远程端的视频的UI
     *
     * @param uid 远程用户的id
     */
    private void setRemoteVideoUI(final int uid) {

        //回掉不能做耗时的操作，所以需要在其他线程运行
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //1,生成远程视图，绑定远程视图
                //小容器,增加视图
                SurfaceView remoteView = RtcEngine.CreateRendererView(getApplicationContext
                        ()); //创建渲染试图
                Log.i("server", "live 的uid" + uid);
                rtcEngine.setupRemoteVideo(new VideoCanvas(remoteView, Constants
                        .RENDER_MODE_HIDDEN, uid));
                //创建对象模型
                SurfaceModel surfaceModel = new SurfaceModel(fl_small1,uid,remoteView,1,1);
                ViewControl.getInstance().addSurfaceView(surfaceModel);
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

}
