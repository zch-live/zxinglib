package com.yzq.zxinglibrary.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.LinearLayoutCompat;
import com.google.zxing.Result;
import com.yzq.zxinglibrary.R;
import com.yzq.zxinglibrary.bean.PhotoView;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.camera.CameraManager;
import com.yzq.zxinglibrary.common.Constant;
import com.yzq.zxinglibrary.decode.DecodeImgCallback;
import com.yzq.zxinglibrary.decode.DecodeImgThread;
import com.yzq.zxinglibrary.decode.ImageUtil;
import com.yzq.zxinglibrary.view.ViewfinderView;
import java.io.IOException;



public class CaptureActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener {

    private static final String TAG = CaptureActivity.class.getSimpleName();
    private ZxingConfig config;
    private SurfaceView preview_view;
    //private Toolbar toolbar;
    private ViewfinderView viewfinder_view;
    private ImageView flashLightIv;
    private ImageView captureImageviewBack;
    private ImageView ivAlbumBackButton;
    private TextView tvAlbumButton;
    private TextView flashLightTv;
    private TextView tv_xc;
    private TextView tvAlbumLoading;
    private LinearLayout flashLightLayout;
    private LinearLayout albumLayout;
    private LinearLayoutCompat bottomLayout;
    private LinearLayoutCompat llc_album;
    private LinearLayoutCompat llc_all;
    private boolean hasSurface;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private SurfaceHolder surfaceHolder;


    public ViewfinderView getViewfinderView() {
        return viewfinder_view;
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public void drawViewfinder() {
        viewfinder_view.drawViewfinder();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//FLAG_FORCE_NOT_FULLSCREEN   FLAG_FULLSCREEN FLAG_TRANSLUCENT_STATUS
        // 保持Activity处于唤醒状态
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_capture);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        /*先获取配置信息*/
        try {
            config = (ZxingConfig) getIntent().getExtras().get(Constant.INTENT_ZXING_CONFIG);
        } catch (Exception e) {
            config = new ZxingConfig();
        }
        initView();
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        beepManager.setPlayBeep(config.isPlayBeep());
        beepManager.setVibrate(config.isShake());
    }


    private void initView() {
        preview_view = (SurfaceView) findViewById(R.id.preview_view);
        preview_view.setOnClickListener(this);
        //toolbar = (Toolbar) findViewById(R.id.toolbar);
        viewfinder_view = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinder_view.setOnClickListener(this);
        flashLightIv = (ImageView) findViewById(R.id.flashLightIv);
        captureImageviewBack = (ImageView) findViewById(R.id.capture_imageview_back);
        captureImageviewBack.setOnClickListener(this);
        captureImageviewBack.setVisibility(View.GONE);
        flashLightTv = (TextView) findViewById(R.id.flashLightTv);
        flashLightLayout = (LinearLayout) findViewById(R.id.flashLightLayout);
        flashLightLayout.setOnClickListener(this);
        albumLayout = (LinearLayout) findViewById(R.id.albumLayout);
        albumLayout.setOnClickListener(this);
        bottomLayout = (LinearLayoutCompat) findViewById(R.id.bottomLayout);
        //搜图模式
        llc_album = (LinearLayoutCompat) findViewById(R.id.llc_album);
        tvAlbumButton = (TextView) findViewById(R.id.tvAlbumButton);
        tv_xc = (TextView) findViewById(R.id.tv_xc);
        tvAlbumLoading = (TextView) findViewById(R.id.tvAlbumLoading);
        tvAlbumButton.setOnClickListener(this);
        ivAlbumBackButton = (ImageView) findViewById(R.id.ivAlbumBackButton);
        ivAlbumBackButton.setOnClickListener(this);
        //扫描模式
        llc_all = (LinearLayoutCompat) findViewById(R.id.llc_all);

        //选图识别模式隐藏其他控件
        if (config.isAlbumModule()){
            switchVisibility(llc_album,config.isAlbumModule());
            switchVisibility(llc_all,!config.isAlbumModule());
            //如果自定义了选图界面则使用自定义的选择相册界面
            if (PhotoView.getPhotoView() != null){
                try {
                    llc_album.removeAllViews();
                    tvAlbumButton = (TextView) PhotoView.getPhotoView().findViewById(R.id.tvAlbumButton);
                    tvAlbumButton.setOnClickListener(this);
                    tvAlbumButton.setVisibility(View.VISIBLE);
                    tvAlbumLoading = (TextView) PhotoView.getPhotoView().findViewById(R.id.tvAlbumLoading);
                    tvAlbumLoading.setVisibility(View.GONE);
                    ivAlbumBackButton = (ImageView) PhotoView.getPhotoView().findViewById(R.id.ivAlbumBackButton);
                    ivAlbumBackButton.setOnClickListener(this);
                    ivAlbumBackButton.setVisibility(View.VISIBLE);
                    ViewGroup parentViewGroup = (ViewGroup) PhotoView.getPhotoView().getParent();
                    if (parentViewGroup != null) {
                        parentViewGroup.removeAllViews();
                    }
                    llc_album.addView(PhotoView.getPhotoView(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                }catch (Exception e){
                    /**
                     * 部分控件id错误
                     * 请检查自定义界面中是否有
                     * tvAlbumButton - TextView
                     * tvAlbumLoading - TextView
                     * ivAlbumBackButton - ImageView
                     * 的控件
                     * */
                    e.printStackTrace();
                }
            }
        }else {
            switchVisibility(bottomLayout, config.isShowbottomLayout());
            switchVisibility(flashLightLayout, config.isShowFlashLight());
            switchVisibility(albumLayout, config.isShowAlbum());
            switchVisibility(captureImageviewBack, config.isShowBack());
//
//        toolbar.setTitle("扫一扫");
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            /*有闪光灯就显示手电筒按钮  否则不显示*/
            if (isSupportCameraLedFlash(getPackageManager())) {
                flashLightLayout.setVisibility(View.VISIBLE);
            } else {
                flashLightLayout.setVisibility(View.GONE);
            }

        }

    }

    /*判断设备是否支持闪光灯*/
    public static boolean isSupportCameraLedFlash(PackageManager pm) {
        if (pm != null) {
            FeatureInfo[] features = pm.getSystemAvailableFeatures();
            if (features != null) {
                for (FeatureInfo f : features) {
                    if (f != null && PackageManager.FEATURE_CAMERA_FLASH.equals(f.name))
                        return true;
                }
            }
        }
        return false;
    }

        /*切换手电筒图片*/

    public void switchFlashImg(int flashState) {

        if (flashState == Constant.FLASH_OPEN) {
            flashLightIv.setImageResource(R.drawable.ic_open);
            flashLightTv.setText("关闭闪光灯");
        } else {
            flashLightIv.setImageResource(R.drawable.ic_close);
            flashLightTv.setText("打开闪光灯");
        }

    }

    /**
     * 扫描成功，处理反馈信息
     *
     * @param rawResult
     */
    public void handleDecode(Result rawResult) {

        inactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();

        Intent intent = getIntent();
        intent.putExtra(Constant.CODED_CONTENT, rawResult.getText());
        //      intent.putExtra(Constant.CODED_BITMAP, barcode);
        setResult(10086, intent);
        this.finish();


    }


    /*切换view的显示*/
    private void switchVisibility(View view, boolean b) {
        if (b) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //选图识别模式不进行初始化等操作
        if (config.isAlbumModule()){

        }else {
            cameraManager = new CameraManager(getApplication());

            viewfinder_view.setCameraManager(cameraManager);
            handler = null;

            surfaceHolder = preview_view.getHolder();
            if (hasSurface) {
                initCamera(surfaceHolder);
            } else {
                // 重置callback，等待surfaceCreated()来初始化camera
                surfaceHolder.addCallback(this);
            }

            beepManager.updatePrefs();
            inactivityTimer.onResume();
        }
    }

    /**
     * 初始化Camera
     *
     * @param surfaceHolder
     */
    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            // 打开Camera硬件设备
            cameraManager.openDriver(surfaceHolder);
            // 创建一个handler来打开预览，并抛出一个运行时异常
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    /**
     * 显示错误信息
     */
    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("扫一扫");
        builder.setMessage(getString(R.string.msg_camera_framework_bug));
        builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }

    @Override
    protected void onPause() {
        //选图识别模式不进行初始化等操作
        if (config.isAlbumModule()){

        }else {
            if (handler != null) {
                handler.quitSynchronously();
                handler = null;
            }
            inactivityTimer.onPause();
            beepManager.close();
            cameraManager.closeDriver();

            if (!hasSurface) {

                surfaceHolder.removeCallback(this);
            }
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    /*点击事件*/
    @Override
    public void onClick(View view) {

        int id = view.getId();
        if (id == R.id.flashLightLayout) {
            /*切换闪光灯*/
            cameraManager.switchFlashLight(handler);
        } else if (id == R.id.albumLayout) {
            /*打开相册*/
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, Constant.REQUEST_IMAGE);
        }else if (id == R.id.tvAlbumButton){
            /*打开相册*/
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, Constant.REQUEST_IMAGE);
        }else if (id == R.id.ivAlbumBackButton){
            /*相册模式返回按钮*/
            inactivityTimer.onActivity();
            beepManager.playBeepSoundAndVibrate();
            finish();
        }else if (id == R.id.capture_imageview_back){
            inactivityTimer.onActivity();
            beepManager.playBeepSoundAndVibrate();
            finish();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constant.REQUEST_IMAGE && resultCode == RESULT_OK) {
            String path = ImageUtil.getImageAbsolutePath(this, data.getData());
            //现在正在扫描中，请稍后回调结果后再说
            tv_xc.setVisibility(View.GONE);
            tvAlbumButton.setVisibility(View.GONE);
            ivAlbumBackButton.setVisibility(View.GONE);
            tvAlbumLoading.setVisibility(View.VISIBLE);
            new DecodeImgThread(path, new DecodeImgCallback() {
                @Override
                public void onImageDecodeSuccess(Result result) {
                    handleDecode(result);
                }

                @Override
                public void onImageDecodeFailed() {
                    handleDecode(new Result("解析失败",null,0,null,null,0));
                    //Toast.makeText(CaptureActivity.this, "抱歉，解析失败,换个图片试试.", Toast.LENGTH_SHORT).show();
                }
            }).run();


        }
    }


}
