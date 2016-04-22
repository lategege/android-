package com.xhl.cocfmas.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2016/4/22.
 */
public class ImageLoader {

    private static Object object = new Object();

    private static ImageLoader imageLoader;

    private int cacheSize = 4 * 1024 * 1024;

    private Handler handler = new Handler();

    //存放图片缓存的
    private LruCache<String, Bitmap> imageCache = new LruCache<>(cacheSize);

    public static ImageLoader getInstance() {
        synchronized (object) {
            if (imageLoader == null) {
                imageLoader = new ImageLoader();
            }
            return imageLoader;
        }
    }

    private ImageLoader() {

    }

    public void showImage(final ImageView imageView, final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = getBitmapFromCache(url);
                if (bitmap == null) {
                    bitmap = getBitmapFromNet(url);
                }
                final Bitmap finalBitmap = bitmap;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(finalBitmap);
                    }
                });
            }
        }).start();

    }


    public Bitmap getBitmapFromCache(String url) {
        String picName = getPicName(url);
        Bitmap bitmap = imageCache.get(picName);
        if (bitmap == null) {
            bitmap = getBitmapFromLocal(url);
        }
        return bitmap;
    }


    private Bitmap getBitmapFromNet(String url) {
        InputStream is = null;
        FileOutputStream os = null;
        try {
            URL net = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) net.openConnection();
            //请求方式GET
            urlConnection.setDoInput(true); //允许输入流，即允许下载
            urlConnection.connect();
            is = urlConnection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            String picName = getPicName(url);

            imageCache.put(picName, bitmap);
            File parentfile = new File("/sdcard" + "/pic/");
            if (!parentfile.exists()) {
                parentfile.mkdirs();
            }
            File file = new File(parentfile, picName + ".jpg");
            if (!file.exists()) {
                file.createNewFile();
            }
            os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,os);
            //存到本地
            return bitmap;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (is != null)
                    is.close();
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getPicName(String url) {
        //存到内存
        String[] tempStr = url.split("/");
        //dsad.jpg
        String[] fileName = tempStr[tempStr.length - 1].split("\\.");
        //dsad
        return fileName[0];
    }

    public Bitmap getBitmapFromLocal(String url) {
        String picName = getPicName(url);
        File file = new File("/sdcard" + "/pic/" + picName + ".jpg");
        if (!file.exists()) {
            return null;
        }
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
