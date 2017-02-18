package com.rl.videocall.appcontorl.bean;

import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * Created by Administrator on 2017/2/18.
 *
 */
public class SurfaceModel {
    private int uid;

    private SurfaceView surfaceView;

    private int status;           //只有2个取值，0:为min   1:为max

    private ViewGroup parent;     //surfaceView 依赖的父容器

    /**
     * 构造方法
     * @param parent
     * @param uid
     * @param surfaceView
     * @param status
     */
    public SurfaceModel(ViewGroup parent, int uid, SurfaceView surfaceView, int status) {
        this.parent = parent;
        this.uid = uid;
        this.surfaceView = surfaceView;
        this.status = status;
    }


    public ViewGroup getParent() {
        return parent;
    }

    public void setParent(ViewGroup parent) {
        this.parent = parent;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public SurfaceView getSurfaceView() {
        return surfaceView;
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SurfaceModel{" +
                "parent=" + parent +
                ", uid=" + uid +
                ", surfaceView=" + surfaceView +
                ", status=" + status +
                '}';
    }
}
