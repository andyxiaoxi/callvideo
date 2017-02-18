package com.rl.videocall.listener;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Administrator on 2017/2/16.
 * 处理触摸事件的对象，（监听对象）
 */
public class OnDoubleTapListener implements View.OnTouchListener {

    private GestureDetector mGestureDetector; //手势对象

    public OnDoubleTapListener(Context context) {
        //1,创建手势对象
        mGestureDetector = new GestureDetector(context,new GestureListener());
    }

    /**
     * 触摸事件的回掉
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mView = v;
        return mGestureDetector.onTouchEvent(event);
    }


    private View mView;
    /**
     * 手势事件的处理类
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        //双击手势
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            OnDoubleTapListener.this.onDoubleTap(mView, e);
            return super.onDoubleTap(e);
        }

        //单机手势
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            OnDoubleTapListener.this.onSingleTapUp();
            return super.onSingleTapUp(e);
        }
    }

    //双击
    public void onDoubleTap(View view, MotionEvent e) {

    }
    //单击
    public void onSingleTapUp() {

    }
}
