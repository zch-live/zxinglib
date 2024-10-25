package com.yzq.zxinglibrary.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import com.google.zxing.Result;
import com.yzq.zxinglibrary.utils.PhotoUtils;
import com.yzq.zxinglibrary.utils.ZXingUtils;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yzq on 2017/10/17.
 * <p>
 * 解析二维码图片
 * 解析是耗时操作，要放在子线程
 */

public class DecodeImgThread extends Thread {


    /*图片路径*/
    private String imgPath;
    /*回调*/
    private DecodeImgCallback callback;

    public DecodeImgThread(String imgPath,DecodeImgCallback callback) {
        this.imgPath = imgPath;
        this.callback = callback;
    }

    @Override
    public void run() {
        super.run();

        if (TextUtils.isEmpty(imgPath) || callback == null) {
            return;
        }

        /**
         * 对图片进行裁剪，防止oom
         */
        /*BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        Bitmap scanBitmap = BitmapFactory.decodeFile(imgPath, options);
        options.inJustDecodeBounds = false; // 获取新的大小
        int sampleSize = (int) (options.outHeight / (float) 400);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        //scanBitmap = BitmapFactory.decodeFile(imgPath, options);
        scanBitmap = openImage(imgPath);
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        // 解码的参数
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>(2);
        // 可以解析的编码类型
        Vector<BarcodeFormat> decodeFormats = new Vector<BarcodeFormat>();
        if (decodeFormats == null || decodeFormats.isEmpty()) {
            decodeFormats = new Vector<BarcodeFormat>();
            // 扫描的类型  一维码和二维码
            decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        // 设置继续的字符编码格式为UTF8
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8");
        // 设置解析配置参数
        multiFormatReader.setHints(hints);*/


        // 开始对图像资源解码
        final Bitmap finalScanBitmap = PhotoUtils.compressPicture(imgPath);
        if (finalScanBitmap == null){
            //防护
            callback.onImageDecodeFailed();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                //解析图片，可能有些图片是一大张，有很多无用因素影响扫描结果
                //处理方法-将图片裁剪，将图片裁剪后的图片在进行扫描
                //如果还不行，那才显示失败
                //逻辑-先扫整张，如果不行，进行分割，分次扫描
                ZXingUtils.syncDecodeQRCode(finalScanBitmap, new ZXingUtils.ResultCallBack() {
                    @Override
                    public void callBack(String result) {
                        callback.onImageDecodeSuccess(new Result(result,null,0,null,null,0));
                    }

                    @Override
                    public void callError() {
                        //传入原图
                        qImage(imgPath,5);
                    }
                });
            }
        }).start();
    }

    /**
     * 走这里意味着首次已经失败了，进行切割处理
     * 首先将原图切割
     * 切割之后在进行采集率压缩
     * */
    private void qImage(final String imgPath,int num){
        Log.e("TAG", "callError:多轮测试-这是第几轮 "+num);
        //拿到整张原图
        Bitmap finalScanBitmap = PhotoUtils.openImage(imgPath);
        // 调用方法将Bitmap切割成8份
        final int[] partsCount = {num};//总切割张数
        final int[] nowCount = {0};//当前张数
        final Boolean[] stopFor = {false}; //是否停止循环，成功了就停止循环了
        final List<Bitmap> bitmapParts = splitBitmapVerticallyIntoNParts(finalScanBitmap, partsCount[0]);
        if (bitmapParts.size() > 0){
            // 现在你可以使用这些部分的Bitmap了
            for (int i = 0; i < bitmapParts.size(); i++) {
                if (!stopFor[0]){
                    final int finalI = i;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //解析图片，可能有些图片是一大张，有很多无用因素影响扫描结果
                            //处理方法-将图片裁剪，将图片裁剪后的图片在进行扫描
                            //如果还不行，那才显示失败
                            //逻辑-先扫整张，如果不行，进行分割，分次扫描 扫描之前进行采集率压缩
                            Bitmap mSrcBitmap = PhotoUtils.compressBySampleSize(bitmapParts.get(finalI),100,100,true);
                            if (mSrcBitmap != null){
                                ZXingUtils.syncDecodeQRCode(mSrcBitmap, new ZXingUtils.ResultCallBack() {
                                    @Override
                                    public void callBack(String result) {
                                        stopFor[0] = true;
                                        callback.onImageDecodeSuccess(new Result(result,null,0,null,null,0));
                                    }

                                    @Override
                                    public void callError() {
                                        nowCount[0] += 1;
                                        if (nowCount[0] == partsCount[0]){
                                            //到达切割数了，还是没成功,只能是失败
                                            stopFor[0] = true;
                                            if (partsCount[0] == 8){
                                                callback.onImageDecodeFailed();
                                            }else {
                                                qImage(imgPath,partsCount[0] += 1);
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }).start();
                }
            }
        }
    }

    /**
     * 将本地图片转成Bitmap
     * @param path 已有图片的路径
     * @return
     */
    public static Bitmap openImage(String path){
        Bitmap bitmap = null;
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
            bitmap = BitmapFactory.decodeStream(bis);
            bis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 将Bitmap垂直切割成指定数量的部分
     * @param bitmap 要切割的Bitmap
     * @param partsCount 切割成的部分数量
     * @return 切割后的Bitmap列表
     */
    private List<Bitmap> splitBitmapVerticallyIntoNParts(Bitmap bitmap, int partsCount) {
        List<Bitmap> bitmapParts = new ArrayList<>();
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int partHeight = height / partsCount;

            for (int i = 0; i < partsCount; i++) {
                int startY = i * partHeight;
                int endY = (i == partsCount - 1) ? height : startY + partHeight; // 确保最后一部分包含剩余的所有像素
                Bitmap part = Bitmap.createBitmap(bitmap, 0, startY, width, endY - startY);
                bitmapParts.add(part);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return bitmapParts;
    }
}
