package com.rl.videocall.config;

import android.util.Log;

import com.rl.videocall.global.RLApplication;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;

/**
 * Created by Administrator on 2017/2/15.
 * 核心类的配置
 */
public class RtcConfig {
    //单例类
    public static final RtcConfig mInstance = new RtcConfig();

    private static final RtcEngine rtcEngine = RLApplication.getRtcEngine();  //核心类

    private RtcConfig(){
    }

    /**
     * 获取配置单利对象
     * @return
     */
    public static RtcConfig getInstance(){
        return mInstance;
    }

    /**
     * 获取rtc对象
     * @return
     */
    public static RtcEngine getRtcEngineInstance(){
        return rtcEngine;
    }

    /**
     * 加入频道前，配置RTC对像
     */
    public static void configRTCPre(){
        if(rtcEngine==null){
            Log.i("dd","rtc对象为空");
            return;
        }
        //1，设置频道属性
        rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION); //对话模式
        //2，开启视频
        rtcEngine.enableVideo();
        //3，设置视频的的属性,240p分辨率，true 宽高交换
        rtcEngine.setVideoProfile(20,true);
    }
}
