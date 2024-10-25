package cn.xz.zxinglib;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView tv3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv3 = findViewById(R.id.tv3);
        findViewById(R.id.tv1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                ZxingConfig config = new ZxingConfig();
                //config.setShowbottomLayout(true);//底部布局（包括闪光灯和相册）
                //config.setPlayBeep(true);//是否播放提示音
                //config.setShake(true);//是否震动
                //config.setShowbottomLayout(true);//底部布局（包括闪光灯和相册）
                //config.setPlayBeep(true);//是否播放提示音
                //config.setShake(true);//是否震动
                config.setShowAlbum(false); //是否显示相册
                //config.setShowFlashLight(true);//是否显示闪光灯
                intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
                startActivityForResult(intent, 100866);
            }
        });
        findViewById(R.id.tv2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                ZxingConfig config = new ZxingConfig();
                //config.setShowbottomLayout(true);//底部布局（包括闪光灯和相册）
                //config.setPlayBeep(true);//是否播放提示音
                //config.setShake(true);//是否震动
                //config.setShowbottomLayout(true);//底部布局（包括闪光灯和相册）
                //config.setPlayBeep(true);//是否播放提示音
                //config.setShake(true);//是否震动
                config.setAlbumModule(true); //相册选图模式
                //config.setShowFlashLight(true);//是否显示闪光灯
                intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
                startActivityForResult(intent, 100866);
            }
        });
        requestPermission();
    }

    /*动态申请权限操作*/
    private boolean isPermissionRequested = false;
    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionRequested) {
            isPermissionRequested = true;
            ArrayList<String> permissionsList = new ArrayList<>();
            String[] permissions = {//在这里加入你要使用的权限
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            };

            for (String perm : permissions) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm)) {
                    permissionsList.add(perm);
                    // 进入这里代表没有权限.
                }
            }

            if (!permissionsList.isEmpty()) {
                String[] strings = new String[permissionsList.size()];
                requestPermissions(permissionsList.toArray(strings), 0);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 10086){
            if (data != null){
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                if (content == null || content.contains("失败")){
                    //识别失败
                    tv3.setText("请提供真实，清晰，完整的条码");
                    //失败了,终极解决办法，判断是不是高版本机型 > android11 ，有没有所有文件访问权限,建议只请求一次
                    // 请求一次不要通过是否已开启访问权限去判断，亲测无效果
                    // 建议通过 本地存储是否请求过
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        //弹窗提示去开启所有文件访问权限
                        Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        startActivity(intent);
                    }

                }else {
                    //识别成功
                    tv3.setText("扫描结果:" + content);
                }
            }
        }
    }
}