package com.kokteyl.android.bumerang.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BumerangWebImage implements BumerangImage {
    private static  int CONNECT_TIMEOUT = 8000;
    private static  int READ_TIMEOUT = 16000;

    private static BumerangWebImageCache bumerangWebImageCache;

    private String url;

    public BumerangWebImage(String url) {
        this.url = url;
    }

    public Bitmap getBitmap(Context context) {
        // Don't leak context
        if(bumerangWebImageCache == null) {
            bumerangWebImageCache = new BumerangWebImageCache(context);
        }

        // Try getting bitmap from cache first
        Bitmap bitmap = null;
        if(url != null) {
            bitmap = bumerangWebImageCache.get(url);
            if(bitmap == null) {
                bitmap = getBitmapFromUrl(url);
                if(bitmap != null){
                    bumerangWebImageCache.put(url, bitmap);
                }
            }
        }

        return bitmap;
    }

    private Bitmap getBitmapFromUrl(String url) {
        Bitmap bitmap = null;
        HttpURLConnection conn = null;
        try {
            URL imageUrl = new URL(url);
            conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.connect();
            InputStream is = (InputStream) conn.getContent();
            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                bitmap = BitmapFactory.decodeStream(is);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }finally {
            if(conn !=null)
                conn.disconnect();
            return bitmap;
        }
    }

    public static void removeFromCache(String url) {
        if(bumerangWebImageCache != null) {
            bumerangWebImageCache.remove(url);
        }
    }
}
