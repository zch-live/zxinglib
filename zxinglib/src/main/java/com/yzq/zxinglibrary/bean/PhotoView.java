package com.yzq.zxinglibrary.bean;

import android.view.View;

/**
 * 动态设置选择相册界面
注意选择按钮要使用 TextView并且id 必须命名为 albumButton 才能被捕获到
注意识别中要使用 TextView并且id 必须命名为 tvLoading 才能被捕获到
 */
public class PhotoView {

    private static View photoView;

    public static void setPhotoView(View view){
        photoView = view;
    }

    public static View getPhotoView(){
        return photoView;
    }
}
