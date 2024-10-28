package com.yzq.zxinglibrary.bean;

import android.view.View;

/**
 * 动态设置选择相册界面
注意打开相册按钮要使用 TextView并且id 必须命名为 tvAlbumButton 才能被捕获到
注意识别中提示要使用 TextView并且id 必须命名为 tvAlbumLoading 才能被捕获到
注意退出按钮要使用 ImageView并且id 必须命名为 ivAlbumBackButton 才能被捕获到
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
