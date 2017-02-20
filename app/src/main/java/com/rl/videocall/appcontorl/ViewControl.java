package com.rl.videocall.appcontorl;

import android.content.Context;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.rl.videocall.appcontorl.bean.SurfaceModel;
import com.rl.videocall.global.RLApplication;
import com.rl.videocall.listener.OnDoubleTapListener;

import java.util.ArrayList;
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
    private List<SurfaceModel> mList = new ArrayList<>();      //数据源

    /**
     * 视图层
     */
    private List<ViewGroup> mViewList;    //那些视图需要被控制

    //私有化构造器
    private ViewControl() {

    }

    private static ViewControl mInstance;

    //rtc对象
    private RtcEngine rtcEngine = RLApplication.getRtcEngine();

    //上下文对象
    private Context context = RLApplication.getGloableContext();

    /**
     * 创建单例对象
     * @param vList，外部传来的容器集合
     */
    public static void createViewControl(List<ViewGroup> vList) {
        if (mInstance == null) {
            mInstance = new ViewControl();
            mInstance.mViewList = vList;
        } else {
            mInstance.mViewList = vList;
        }

        //设置容器的值
        setContainer(mInstance);
    }

    private RelativeLayout big_container;      //大容器
    private FrameLayout small_1;                //小容器
    private FrameLayout small_2;               //小容器

    /**
     * 容器集合是按顺序存储的。
     */
    private static void setContainer(ViewControl mInstance) {
        if (mInstance.mViewList == null) {
            throw new RuntimeException("The view container is null.");
        }
        if (mInstance.mViewList.size() != 3) {
            throw new RuntimeException("The container size is not 3.");
        }
        mInstance.big_container = (RelativeLayout) mInstance.mViewList.get(0);
        mInstance.small_1 = (FrameLayout) mInstance.mViewList.get(1);
        mInstance.small_2 = (FrameLayout) mInstance.mViewList.get(2);
    }

    /**
     * 获取单利对象
     *
     * @return
     */
    public static ViewControl getInstance() {
        if (mInstance == null) {
            throw new RuntimeException("please invoke createViewControl method at first");
        } else {
            return mInstance;
        }
    }


    //监听数据增加的事件--激发控制ui


    //监听数据减少的事件----激发控制ui


    //自身改变的事件----激发控制ui


    //ui改变方法

    /**
     * 增加view
     *
     * @param surfaceModel
     */
    public void addSurfaceView(SurfaceModel surfaceModel) {
        //1,存入到数据源
        mList.add(surfaceModel);
        //2,数据改变，触发ui重设置
        setUI();
    }

    private void setUI() {
        //1,根据集合的个数，设置ui
        if (mList.size() == 0) {
            return;
        }
        if (mList.size() == 1) { //此时，只剩下一个本地surfaceView
            setLocalUI(mList.get(0));
        }
        if (mList.size() == 2) {
            setTwoUI();           // 设置2个view的视图
            setDoubleClickListener();  //在视图布局好，在注册监听
        }
        if(mList.size()==3){ //设置3个view的视图
            setThreeView();
            setDoubleClickListener();  //在视图布局好，在注册监听
        }

    }


    /**
     * 根据模型的字段值，设置本地ui
     *
     * @param surfaceModel
     */
    private void setLocalUI(SurfaceModel surfaceModel) {
        //1,先判断下该view 是否有父容器
        if (surfaceModel.getParent() == null) { //默认布局
            SurfaceView surfaceView = surfaceModel.getSurfaceView();
            //1,设置view显示在底层
            surfaceView.setZOrderOnTop(false);
            //2，设置view的布局参数
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup
                    .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            //3,布局到容器
            big_container.addView(surfaceView);
            //4,绑定父容器
            surfaceModel.setParent(big_container);
            return;
        } else {//有父容器
            //根据视图大小的标志设置布局参数
            if (surfaceModel.getStatus() == 0) {
                surfaceModel.getSurfaceView().setZOrderOnTop(true);
            } else if (surfaceModel.getStatus() == 1) {
                surfaceModel.getSurfaceView().setZOrderOnTop(false);
            }

            //根据父容器字段，设置ui
            if (surfaceModel.getParent() == big_container) {
                big_container.addView(surfaceModel.getSurfaceView());
            } else if (surfaceModel.getParent() == small_1) {
                small_1.addView(surfaceModel.getSurfaceView());
            } else if (surfaceModel.getParent() == small_2) {
                small_2.addView(surfaceModel.getSurfaceView());
            }

        }

    }

    /**
     * 当集合为2时，设置ui
     */
    private void setTwoUI() {
        //1,遍历集合
        for (int i = 0; i < mList.size(); i++) {
            SurfaceModel temp = mList.get(i);
            //2,根据字段的内容设置ui
            //2.1 先设置本地view
            if (i == 0) { //设置本地ui
                //从父容器移除
                removeFromViewGroup(temp);
                //重新设置绑定父容器，视图大小的状态
                temp.setParent(small_1);
                temp.setStatus(0);
                setLocalUI(temp);
            } else if (i == 1) { //设置远程ui
                removeFromViewGroup(temp);
                //重新设置绑定父容器，视图大小的状态
                temp.setParent(big_container);
                temp.setStatus(1);
                setRemoteUI(temp);
            }
        }
    }

    /**
     * 从父容器移除
     *
     * @param surfaceModel
     */
    //从父容器移除
    private void removeFromViewGroup(SurfaceModel surfaceModel) {
        if (surfaceModel.getParent() == null) {
            return;
        }
        if (surfaceModel.getParent() == big_container) {
            big_container.removeView(surfaceModel.getSurfaceView());
        }
        if (surfaceModel.getParent() == small_1) {
            small_1.removeView(surfaceModel.getSurfaceView());
        }
        if (surfaceModel.getParent() == small_2) {
            small_2.removeView(surfaceModel.getSurfaceView());
        }
        //清除view的父容器
        surfaceModel.setParent(null);

    }

    /**
     * 设置远程ui
     */
    private void setRemoteUI(SurfaceModel surfaceModel) {
        //根据视图大小的标志设置布局参数
        if (surfaceModel.getStatus() == 0) {
            surfaceModel.getSurfaceView().setZOrderOnTop(true);
        } else if (surfaceModel.getStatus() == 1) {
            surfaceModel.getSurfaceView().setZOrderOnTop(false);
        }

        //根据父容器字段，设置ui
        if (surfaceModel.getParent() == big_container) {
            big_container.addView(surfaceModel.getSurfaceView());
        } else if (surfaceModel.getParent() == small_1) {
            small_1.addView(surfaceModel.getSurfaceView());
        } else if (surfaceModel.getParent() == small_2) {
            small_2.addView(surfaceModel.getSurfaceView());
        }
    }


    //改变ui,只给small_1 ,small_2 设置监听

    public void setDoubleClickListener() {
        small_1.setOnTouchListener(new OnDoubleTapListener(context) {
            //双击回掉
            @Override
            public void onDoubleTap(View view, MotionEvent e) {
                //设置ui
                //1,先找到small_1，big_container 容器对应的model
                SurfaceModel big_model = findView(big_container);
                SurfaceModel sm1_model = findView(small_1);
                //2,更改模型的，父容器和试图大小状态，重新设置ui
                //2.1 分别,解除绑定
                removeFromViewGroup(big_model);
                removeFromViewGroup(sm1_model);
                //2.2 绑定父容器和改变视图大小
                big_model.setParent(small_1);
                big_model.setStatus(0);
                setRemoteUI(big_model);

                sm1_model.setParent(big_container);
                sm1_model.setStatus(1);
                setLocalUI(sm1_model);

            }
        });

        if(mList.size()==3){ //,给小容器2,设置监听
            small_2.setOnTouchListener(new OnDoubleTapListener(context){
                @Override
                public void onDoubleTap(View view, MotionEvent e) {
                    //设置ui
                    //1,先找到small_2，big_container 容器对应的model
                    SurfaceModel big_model = findView(big_container);
                    SurfaceModel sm2_model = findView(small_2);
                    //2,更改模型的，父容器和试图大小状态，重新设置ui
                    //2.1 分别,解除绑定
                    removeFromViewGroup(big_model);
                    removeFromViewGroup(sm2_model);
                    //2.2 绑定父容器和改变视图大小
                    big_model.setParent(small_2);
                    big_model.setStatus(0);
                    setRemoteUI(big_model);

                    sm2_model.setParent(big_container);
                    sm2_model.setStatus(1);
                    setLocalUI(sm2_model);
                }
            });
        }
    }

    /**
     * 通过容器类，找到对应的surfaceModel模型
     *
     * @param group
     * @return
     */
    private SurfaceModel findView(ViewGroup group) {
        SurfaceModel result = null;
        for (int i = 0; i < mList.size(); i++) {
            SurfaceModel temp = mList.get(i);
            if (temp.getParent() == group) {
                result = temp;
            }
        }
        if (result == null) {
            throw new RuntimeException("no find surfaceModel object");
        }
        return result;
    }


    //根据uid,移除对应视图
    public void deleteSurfaceView(int uid) {
        SurfaceModel surfaceModel = findUidSurfaceModel(uid);
        //1,视图的移除
        removeFromViewGroup(surfaceModel);
        //取消监听
        // surfaceModel.getParent().setOnTouchListener(null);

        //2,数据源的删除
        mList.remove(surfaceModel);

        //3，根据集合size,重新设置ui
        if (mList.size() == 1) {
            small_1.setOnTouchListener(null);   //清除1监听
            SurfaceModel l_model = mList.get(0);
            removeFromViewGroup(l_model);
            l_model.setParent(big_container);
            l_model.setStatus(1);
            setLocalUI(l_model);
        }
        if (mList.size() == 2) {
            small_2.setOnTouchListener(null);   //清除2监听
            //重新设置ui
            setTwoUI();
        }
    }

    /**
     * 通过uid，找到surfaceModel
     *
     * @param uid
     * @return
     */
    private SurfaceModel findUidSurfaceModel(int uid) {
        SurfaceModel result = null;
        for (int i = 0; i < mList.size(); i++) {
            SurfaceModel temp = mList.get(i);
            if (temp.getUid() == uid) {
                result = temp;
            }
        }
        if (result == null) {
            throw new RuntimeException("no find SurfaceModel object via uid");
        }
        return result;
    }

    //3个视图的设置
    private void setThreeView(){
        SurfaceModel surfaceModel = mList.get(2); //一定会存到small_2中
        //重新设置model的属性
        //清除绑定
        removeFromViewGroup(surfaceModel);
        surfaceModel.setParent(small_2);
        surfaceModel.setStatus(0);
        setRemoteUI(surfaceModel);
    }
}
