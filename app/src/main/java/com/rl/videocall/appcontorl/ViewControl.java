package com.rl.videocall.appcontorl;

import android.content.Context;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.rl.videocall.appcontorl.bean.SurfaceModel;
import com.rl.videocall.global.RLApplication;

import java.util.List;

import io.agora.rtc.RtcEngine;

/**
 * Created by Administrator on 2017/2/18.
 * 视图控制器，单例类
 */
public class ViewControl {
    /**
     * 数据层
     */
    private List<SurfaceModel> mList;       //数据源

    /**
     * 视图层
     */
    private List<ViewGroup> mViewList;    //那些视图需要被控制
    //私有化构造器
    private ViewControl(){

    }

    private static ViewControl mInstance;

    //rtc对象
    private RtcEngine rtcEngine= RLApplication.getRtcEngine();

    //上下文对象
    private Context context = RLApplication.getGloableContext();

    /**
     * 创建单例对象
     * @param dlist
     * @param vList
     */
    public static void createViewControl(List<SurfaceModel> dlist, List<ViewGroup> vList){
        if(mInstance ==null){
            mInstance = new ViewControl();
            mInstance.mList = dlist;
            mInstance.mViewList = vList;
        }else{
            mInstance.mList = dlist;
            mInstance.mViewList = vList;
        }
    }

    /**
     * 获取单利对象
     * @return
     */
    public static ViewControl getInstance(){
        if(mInstance==null){
            throw new RuntimeException("please invoke createViewControl method at first");
        }else {
            return mInstance;
        }
    }


    //监听数据增加的事件--激发控制ui



    //监听数据减少的事件----激发控制ui



    //自身改变的事件----激发控制ui


    //ui改变方法


    private void addSurfaceView(int uid){
        SurfaceView surfaceView = RtcEngine.CreateRendererView(context);   //创建渲染视图
        mList.add(new SurfaceModel(null,uid,surfaceView,0));               //添加到数据源
        //遍历数据集合
    }

}
