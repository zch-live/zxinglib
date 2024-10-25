package com.yzq.zxinglibrary.utils;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 拍照，相册，录像等
 */
public class PhotoUtils {
    public static int CAMERA_REQUEST_CODE = 0;
    public static int GALLERY_REQUEST_CODE = 1;
    public static int RECORD_REQUEST_CODE = 2;
    public static Uri mImageUri;
    public static String imageFilePath = "DCIM/card.jpg";


    /**
     * 拍照
     * @param activity
     */
    public static void takePhoto(Activity activity){
        // 跳转到系统的拍照界面
        Intent intentToTakePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 指定照片存储位置为sd卡本目录下
        // 这里设置为固定名字 这样就只会只有一张temp图 如果要所有中间图片都保存可以通过时间或者加其他东西设置图片的名称
        // File.separator为系统自带的分隔符 是一个固定的常量
        String tempPhotoPath = Environment.getExternalStorageDirectory() + File.separator + imageFilePath;
        // 获取图片所在位置的Uri路径    *****这里为什么这么做参考问题2*****
        /*imageUri = Uri.fromFile(new File(mTempPhotoPath));*/
        mImageUri = FileProvider.getUriForFile(activity,
                activity.getPackageName() +".my.provider",
                new File(tempPhotoPath));
        //下面这句指定调用相机拍照后的照片存储的路径
        intentToTakePhoto.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        activity.startActivityForResult(intentToTakePhoto, CAMERA_REQUEST_CODE);

    }

    /**
     * 拍照
     * @param fragment
     */
    public static void takePhoto(Fragment fragment){
        // 跳转到系统的拍照界面
        Intent intentToTakePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 指定照片存储位置为sd卡本目录下
        // 这里设置为固定名字 这样就只会只有一张temp图 如果要所有中间图片都保存可以通过时间或者加其他东西设置图片的名称
        // File.separator为系统自带的分隔符 是一个固定的常量
        String tempPhotoPath = Environment.getExternalStorageDirectory() + File.separator + imageFilePath;
        // 获取图片所在位置的Uri路径    *****这里为什么这么做参考问题2*****
        /*imageUri = Uri.fromFile(new File(mTempPhotoPath));*/
        mImageUri = FileProvider.getUriForFile(fragment.getActivity(),
                fragment.getActivity().getPackageName() +".my.provider",
                new File(tempPhotoPath));
        //下面这句指定调用相机拍照后的照片存储的路径
        intentToTakePhoto.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        fragment.startActivityForResult(intentToTakePhoto, CAMERA_REQUEST_CODE);
    }



    /**
     * 相册选择
     * @param activity
     */
    public static void choosePhoto(Activity activity) {
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
        // 如果限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型" 所有类型则写 "image/*"
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        activity.startActivityForResult(intentToPickPic,GALLERY_REQUEST_CODE);
    }

    /**
     * 相册选择
     * @param fragment
     */
    public static void choosePhoto(Fragment fragment) {
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
        // 如果限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型" 所有类型则写 "image/*"
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        fragment.startActivityForResult(intentToPickPic,GALLERY_REQUEST_CODE);
    }


    /**
     * 录像
     * @param fragment
     */
    private static void recordVideo(Fragment fragment) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,5);
        fragment.startActivityForResult(intent,RECORD_REQUEST_CODE);
    }

    /**
     * 录像
     * @param activity
     */
    private static void recordVideo(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,5);
        activity.startActivityForResult(intent,RECORD_REQUEST_CODE);
    }

    /**
     * 根据uri获取图片路径
     * @param context
     * @param uri
     * @return
     */
    public static String getPicturePathFromUri(Context context, Uri uri) {
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= 19) {
            return getPicturePathFromUriAboveApi19(context, uri);
        } else {
            return getPicturePathFromUriBelowAPI19(context, uri);
        }
    }

    private static String getPicturePathFromUriBelowAPI19(Context context, Uri uri) {
        return getDataColumn(context, uri, null, null);
    }

    private static String getPicturePathFromUriAboveApi19(Context context, Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) { // MediaProvider
                // 使用':'分割
                String id = documentId.split(":")[1];

                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = {id};
                filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是 content 类型的 Uri
            filePath = getDataColumn(context, uri, null, null);
        } else if ("file".equals(uri.getScheme())) {
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            filePath = uri.getPath();
        }
        return filePath;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;

        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * 压缩图片
     * @param imgPath
     * @return
     */
    public static Bitmap compressPicture(String imgPath) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imgPath, options);
            options.inSampleSize = calculateInSampleSize(options, 100, 100);
            options.inJustDecodeBounds = false;
            Bitmap afterCompressBm = BitmapFactory.decodeFile(imgPath, options);
            return afterCompressBm;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }


    /**
     * 第二种：按采样大小压缩
     *
     * @param src       源图片
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @param recycle   是否回收
     * @return 按采样率压缩后的图片
     */
    public static Bitmap compressBySampleSize(final Bitmap src, final int maxWidth, final int maxHeight, final boolean recycle) {
        if (src == null || src.getWidth() == 0 || src.getHeight() == 0) {
            return null;
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            src.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;
            if (recycle && !src.isRecycled()) {
                src.recycle();
            }
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            return bitmap;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
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
}