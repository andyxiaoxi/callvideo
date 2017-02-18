package com.rl.videocall.rtcevent;

import android.util.Log;
import android.view.SurfaceView;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import io.agora.rtc.IRtcEngineEventHandler;

/**
 * Created by Administrator on 2017/2/15.
 */
public class RLRtcEventHandler extends IRtcEngineEventHandler {
    private MyEventHandler mMyEventHandler;
    private static final String TAG = RLRtcEventHandler.class.getSimpleName();

    //远程端，第一帧解码成功的回掉，设置远程端显示的view
    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        super.onFirstRemoteVideoDecoded(uid, width, height, elapsed);

        Log.i(TAG,"远程解码："+"宽："+width+"-----高："+height);
        //远程端的视频显示的设置
        if (mMyEventHandler != null) {
            //用来给监听者，设置远程用户ui
            mMyEventHandler.onFirstRemoteVideoDecoded(uid);
        }
    }

    //客户端加入频道的回掉
    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        super.onJoinChannelSuccess(channel, uid, elapsed);
        Log.i(TAG, "加入频道成功,本地用户ID:"+uid);
    }

    //客户端重新加入频道
    @Override
    public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
        super.onRejoinChannelSuccess(channel, uid, elapsed);
    }

    //离开频道的回掉
    @Override
    public void onLeaveChannel(RtcStats stats) {
        super.onLeaveChannel(stats);
    }

    //说话声音提示回掉
    @Override
    public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
        super.onAudioVolumeIndication(speakers, totalVolume);
    }

    //其他用户加入频到
    @Override
    public void onUserJoined(int uid, int elapsed) {
        super.onUserJoined(uid, elapsed);
        Log.i(TAG,"有其他用户加入频道");
    }

    //其他用户离开频道的回掉
    @Override
    public void onUserOffline(int uid, int reason) {
        if (mMyEventHandler != null) {
            //用来给监听者，设置远程用户ui
            mMyEventHandler.onUserOffline(uid,reason);
        }
        super.onUserOffline(uid, reason);
    }

    //用户静音回掉
    @Override
    public void onUserMuteAudio(int uid, boolean muted) {
        super.onUserMuteAudio(uid, muted);
    }

    //用户视频回掉
    @Override
    public void onUserMuteVideo(int uid, boolean muted) {
        super.onUserMuteVideo(uid, muted);
    }

    //本地用户网络质量的回掉
    @Override
    public void onLastmileQuality(int quality) {
        super.onLastmileQuality(quality);
    }

    //连接中断回掉
    @Override
    public void onConnectionInterrupted() {
        super.onConnectionInterrupted();
    }

    //本地视频统计回调
    @Override
    public void onLocalVideoStats(LocalVideoStats stats) {
        super.onLocalVideoStats(stats);
    }

    //警告
    @Override
    public void onWarning(int warn) {
        super.onWarning(warn);
    }

    /**
     * 注册监听
     *
     * @param l
     */
    public void setMyEventHandlerListener(MyEventHandler l) {
        mMyEventHandler = l;
    }

    /**
     * 移除监听
     */
    public void removeMyEventHandlerListener() {
        mMyEventHandler = null;
    }

    /**
     * rtc 回掉接口
     */
    public interface MyEventHandler {
        void onFirstRemoteVideoDecoded(int uid);

        /**
         * 有用户离开频道
         * @param uid
         * @param reason
         */
        void onUserOffline(int uid ,int reason);
    }
}
