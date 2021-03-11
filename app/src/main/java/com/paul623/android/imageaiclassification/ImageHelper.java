package com.paul623.android.imageaiclassification;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.icu.text.SimpleDateFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class ImageHelper {
    public static String coverFromBitmap(Context context, String path) {
        Bitmap bitmap = SuperSuitWay(context, path);
        if (bitmap == null) {
            return "";
        }
        return compressImage(context, bitmap).getAbsolutePath();
    }

    public static File compressImage(Context context, Bitmap bitmap) {
        String filename;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        //图片名
        filename = format.format(date);
        final File[] dirs = context.getExternalFilesDirs("Documents");
        File primaryDir = null;
        if (dirs != null && dirs.length > 0) {
            primaryDir = dirs[0];
        }
        File file = new File(primaryDir.getAbsolutePath(), filename + ".png");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            try {
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }

        // recycleBitmap(bitmap);
        return file;
    }

    public static Bitmap SuperSuitWay(Context context, String path) {
        Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        try {
            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                    new String[]{path}, null);
            Uri imageUri = null;
            if (cursor != null && cursor.moveToFirst()) {
                imageUri = ContentUris.withAppendedId(external, cursor.getLong(0));
                cursor.close();
            }
            ParcelFileDescriptor pfd = null;
            if (imageUri != null) {
                try {
                    pfd = context.getContentResolver().openFileDescriptor(imageUri, "r");
                    if (pfd != null) {
                        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                        return bitmap;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (pfd != null) {
                            pfd.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }catch (Exception e){
            return null;
        }
        return null;
    }

    /**
     * 获取照片属性中的旋转角度
     *
     * @param path 图片的绝对路径
     * @return 照片属性中的旋转角度
     */
    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public static int getOrientationRotate(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其Exif信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 根据角度值旋转Bitmap
     *
     * @param bitmap
     * @param degree
     * @return
     */
    private static Bitmap rotateBitmapByDegree(Bitmap bitmap, int degree) {

        // 根据旋转角度，得到旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        // 将原始图片按照旋转矩阵进行旋转，得到新的图片
        Bitmap result = null;
        try {
            result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            result = bitmap;
        }

        if (bitmap != result) {
            bitmap.recycle();
        }
        return result;
    }

    public static Bitmap getBitmap(Context context,String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return bitmap;
        }
        return rotateBitmapByDegree(bitmap, getOrientationRotate(path));
    }
    /**
     * 按新的宽高缩放图片
     * @param bm
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap scaleImage(Bitmap bm, int newWidth, int newHeight)
    {
        if (bm == null)
        {
            return null;
        }
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                true);
        if (bm != null & !bm.isRecycled())
        {
            bm.recycle();
            bm = null;
        }
        return newbm;
    }

    /**
     * 通过path来获取图片
     * 适配Q
     * @param context 上下文
     * @param path 相对路径（在安卓Q下会失效）
     * */
    public static Bitmap getBitmapByPath(Context context,String path){
        if(path==null||path.equals("")){
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_error_loading);
        }
        //适配安卓Q
        //由于安卓Q采用了沙盒模式，故必须根据Uri来加载图片
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            return getBitmapFromUri(context,getImageContentUri(context,path));
        }else {
            return getBitmapFromSrc(path);
        }
    }
    /**
     * 适配安卓Q
     * 拿到图片地址后转换
     * */
    private static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 适配安卓Q
     * 由地址获得URI
     * */
    private static Uri getImageContentUri(Context context, String path) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID }, MediaStore.Images.Media.DATA + "=? ",
                new String[] { path }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            // 如果图片不在手机的共享图片数据库，就先把它插入。
            if (new File(path).exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, path);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
    public static Bitmap getBitmapFromSrc(String src){
        return scaleImage(BitmapFactory.decodeFile(src),224,224);
    }

}
