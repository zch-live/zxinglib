package com.yzq.zxinglibrary.bean;

import android.view.View;

import java.io.Serializable;

/**
 * Created by yzq on 2017/10/19.
 * <p>
 * zxing配置类
 */

public class ZxingConfig implements Serializable {


    /*是否播放声音*/
    private boolean isPlayBeep = true;
    /*是否震动*/
    private boolean isShake = false;
    /*是否显示下方的其他功能布局*/
    private boolean isShowbottomLayout = true;
    /*是否显示扫描模式下左上角返回按钮*/
    private boolean isShowBack = false;
    /*是否显示闪光灯按钮*/
    private boolean isShowFlashLight = true;
    /*是否显示相册按钮*/
    private boolean isShowAlbum = true;
    /*相册选图模式，其他界面全部隐藏*/
    private boolean isAlbumModule = false;
    /*自定义选择相册布局*/
    private View photoView = null;

    public boolean isPlayBeep() {
        return isPlayBeep;
    }

    public void setPlayBeep(boolean playBeep) {
        isPlayBeep = playBeep;
    }

    public boolean isShake() {
        return isShake;
    }

    public void setShake(boolean shake) {
        isShake = shake;
    }

    public boolean isShowbottomLayout() {
        return isShowbottomLayout;
    }

    public void setShowbottomLayout(boolean showbottomLayout) {
        isShowbottomLayout = showbottomLayout;
    }

    public boolean isShowFlashLight() {
        return isShowFlashLight;
    }

    public void setShowFlashLight(boolean showFlashLight) {
        isShowFlashLight = showFlashLight;
    }

    public boolean isShowAlbum() {
        return isShowAlbum;
    }

    public void setShowAlbum(boolean showAlbum) {
        isShowAlbum = showAlbum;
    }

    public boolean isAlbumModule() {
        return isAlbumModule;
    }

    public void setAlbumModule(boolean albumModule) {
        isAlbumModule = albumModule;
    }

    public View getPhotoView() {
        return photoView;
    }

    public void setPhotoView(View photoView) {
        this.photoView = photoView;
    }

    public boolean isShowBack() {
        return isShowBack;
    }

    public void setShowBack(boolean showBack) {
        isShowBack = showBack;
    }
}
