package com.rl.videocall.global;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.rl.videocall.callvideo.R;
import com.rl.videocall.rtcevent.RLRtcEventHandler;

import io.agora.rtc.RtcEngine;

/**
 * Created by Administrator on 2017/2/15.
 * 全局配置类
 */
public class RLApplication extends Application {
    public static RtcEngine rtcEngine;  //核心类
    public static Context Gcontext;
    public static Handler mainHandler;
    public static RLRtcEventHandler mRlRtcEventHandler;                //rtc事件的回调
    String appid="";    //官网申请的id
    @Override
    public void onCreate() {
        super.onCreate();
        Gcontext = getApplicationContext();
        mainHandler = new Handler();
        //设置appid
        appid = Gcontext.getString(R.string.app_id);
        mRlRtcEventHandler = new RLRtcEventHandler();
        createRTCengine();
    }

    /**
     * 创建rtc核心对象
     */
    public void createRTCengine() {
        rtcEngine = RtcEngine.create(Gcontext,appid,mRlRtcEventHandler);
    }

    /**
     * 获取rtc对象
     *
     * @return
     */
    public static RtcEngine getRtcEngine() {
        if (rtcEngine == null) {
            throw new RuntimeException("rtcEngine is null");
        }
        return rtcEngine;
    }

    /**
     * 获取handler
     * @return
     */
    public static Handler getMainHandler(){
        return mainHandler;
    }

    /**
     * 获取全局context对象
     * @return
     */
    public static Context getGloableContext(){
        if(Gcontext==null){
            throw new RuntimeException("Gcontext is null..");
        }
        return Gcontext;
    }

    /**
     * 获取rtc回掉对象
     * @return
     */
    public static RLRtcEventHandler getRLRtcEventHandler(){
        if(mRlRtcEventHandler==null){
            throw new RuntimeException("Gcontext is null..");
        }
        return mRlRtcEventHandler;
    }
}
