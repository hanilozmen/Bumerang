package com.kokteyl.android.bumerang.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import com.kokteyl.android.bumerang.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
* Written by hanilozmen
*
* It is written by hanilozmen completely.
*
* */
public class BumerangImageLoader {

    private static final String CACHE_DIR_NAME = "qumpara_sdk_cache";
    private static final int CONNECT_TIMEOUT = 10 * 1000;
    private static final int READ_TIMEOUT = 15 * 1000;
    private static final int IMAGE_SIZE_MIN = 500;
    private static int ANIMATION_DURATION = 300;

    private final static Object singletonLock = new Object();

    public static class Core {
        protected ExecutorService executorService;
        protected static Core mInstance;
        protected MemoryCache memoryCache;
        protected FileCache fileCache;
        protected Map<ImageView, String> imageViews;
        protected ImageListener imageListener;

        public void clear() {
            try {
                mInstance = null;
                executorService.shutdown();
                memoryCache.clear();
                imageViews.clear();
                imageListener = null;

            }catch (Exception e) {

            }
        }

        public static Core getInstance(Context context) {
            if(mInstance == null) {
                synchronized (singletonLock) {
                    mInstance = new Core(context);
                    return mInstance;
                }
            }else {
                return mInstance;
            }
        }

        private Core(Context context) {
            memoryCache =  new MemoryCache();
            fileCache = new FileCache(context);
            imageViews  = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
            executorService = new ThreadPoolExecutor(10, 50,10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(20));
            imageListener = new ImageListener() {
                @Override
                public void success(String url) {
                    if(!imageViews.containsValue(url) || url == null) return;
                    for (Entry<ImageView, String> entry : imageViews.entrySet()) {
                        if(url.equals(entry.getValue())){
                            final Bitmap bitmap = memoryCache.get(url);
                            final ImageView imageView = entry.getKey();
                            if( imageView == null|| bitmap ==null)
                                continue;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    imageViews.remove(imageView);
                                    imageView.setImageBitmap(bitmap);
                                }
                            });
                        }
                    }
                }

                @Override
                public void fail(String url) {
                    if(!imageViews.containsValue(url) || url == null) {
                        Log.i("ANIL","fail url not found: " + url);
                        return;
                    }
                    for (Entry<ImageView, String> entry : imageViews.entrySet()) {
                        Log.i("ANIL",entry.getValue());
                        if(url.equals(entry.getValue())){
                            ImageView imageView = entry.getKey();
                            if( imageView == null)
                                continue;
                            imageView.setImageResource(R.drawable.uncomplete_img);
                        }
                    }
                }
            };
        }

        public enum ImageAnimation {
            NO_ANIMATION,
            FADE_IN
        }

        //TODO final int stub_id=R.drawable.ic_launcher;
        private void DisplayImage(String url, ImageView imageView) {
            imageViews.put(imageView, url);
            Bitmap bitmap = memoryCache.get(url);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else if(memoryCache.cache != null && memoryCache.cache.containsKey(url)) {
                //waiting to be load by another imageview. Skip
            }else {
                memoryCache.put(url, null);
                queuePhoto(url, imageView, null);
            }
        }

        public void DisplayImage(String url, ImageView imageView, boolean fadeIn) {
            if(fadeIn)
                displayImageWithAnimation(url, imageView, ImageAnimation.FADE_IN);
            else
                DisplayImage(url, imageView);
        }

        public void DisplayImage(String url, ImageView imageView, ImageAnimation animation, int animationDuration) {
            ANIMATION_DURATION = animationDuration;
            displayImageWithAnimation(url, imageView, animation);
        }

        public void DisplayImage(String url, ImageView imageView, ImageAnimation animation) {
            displayImageWithAnimation(url, imageView, animation);
        }

        public void displayImageWithAnimation(String url, ImageView imageView, ImageAnimation animation) {
            try {
                imageViews.put(imageView, url);
                Bitmap bitmap = memoryCache.get(url);
                if (bitmap != null) {
                    animateImageView(imageView, bitmap, animation);
                } else {
                    queuePhoto(url, imageView, animation);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void animateImageView(ImageView imageView, Bitmap bitmap, ImageAnimation animation) {
            if (animation == null) {
                imageView.setImageBitmap(bitmap);
                return;
            }
            switch (animation) {
                case FADE_IN:
                    imageView.setAlpha(0f);
                    imageView.setImageBitmap(bitmap);
                    imageView.animate().setDuration(ANIMATION_DURATION).alpha(1f).start();
                    break;
                default:
                    imageView.setImageBitmap(bitmap);
                    break;
            }
        }

        private void queuePhoto(String url, ImageView imageView, ImageAnimation animation) {
            PhotoToLoad p = new PhotoToLoad(url, imageView);
            try {
                executorService.submit(new PhotosLoader(p, animation));
            }catch (Exception e) {
                e.printStackTrace();
            }

        }

        private Bitmap getBitmap(String url) {
            File f = fileCache.getFile(url);
            // from local cache directory
            Bitmap b = decodeFile(f);
            if (b != null)
                return b;
            //from web
            HttpURLConnection conn = null;
            Bitmap bitmap = null;
            try {
                URL imageUrl = new URL(url);
                conn = (HttpURLConnection) imageUrl.openConnection();
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setDoInput(true);
                conn.connect();
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    OutputStream os = new FileOutputStream(f);
                    CopyStream(is,os);
                    os.close();
                    bitmap = decodeFile(f);
                }
            } catch (Throwable ex) {
                if (ex instanceof OutOfMemoryError) {
                    memoryCache.clear();
                    imageViews.clear();
                }
                if(ex instanceof SocketTimeoutException){
                    Log.w("QUMPARA_OFFERWALL_IMAGE", String.format(Locale.ENGLISH,"Socket timeout exception while downloading: %s", url));
                }
                return null;
            }finally {
                if(conn!= null)
                    conn.disconnect();
                return bitmap;
            }
        }

        //decodes image and scales it to reduce memory consumption
        private Bitmap decodeFile(File f) {
            try {
                if (!f.exists()) {
                    return null;
                }
                //decode image size
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;

                int width_tmp = o.outWidth, height_tmp = o.outHeight;
                int scale = 1;
                while (true) {
                    if (width_tmp / 2 < IMAGE_SIZE_MIN || height_tmp / 2 < IMAGE_SIZE_MIN)
                        break;
                    width_tmp /= 2;
                    height_tmp /= 2;
                    scale *= 2;
                }

                //decode with inSampleSize
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        //Task for the queue
        private static class PhotoToLoad {
            public String url;
            public ImageView imageView;

            public PhotoToLoad(String u, ImageView i) {
                url = u;
                imageView = i;
            }
        }

        class PhotosLoader implements Runnable {
            PhotoToLoad photoToLoad;
            ImageAnimation animation;

            PhotosLoader(PhotoToLoad photoToLoad, ImageAnimation animation) {
                this.photoToLoad = photoToLoad;
                this.animation = animation;
            }

            @Override
            public void run() {
                if (imageViewReused(photoToLoad))
                    return;
                Bitmap bmp = getBitmap(photoToLoad.url);
                if(photoToLoad.imageView.getContext() == null) return;
                if(bmp != null ) {
                    memoryCache.put(photoToLoad.url, bmp);
                    Core.getInstance(photoToLoad.imageView.getContext()).imageListener.success(photoToLoad.url);
                }else {
                    Core.getInstance(photoToLoad.imageView.getContext()).imageListener.fail(photoToLoad.url);
                }

            }
        }

        boolean imageViewReused(PhotoToLoad photoToLoad) {
            String tag = imageViews.get(photoToLoad.imageView);
            if (tag == null || !tag.equals(photoToLoad.url)) {
                Log.i("ANIL", "imageView reused tag: "  + tag);
                return true;
            }
            Log.i("ANIL", "imageView not reused: "  + tag);
            return false;
        }

        //Used to display bitmap in the UI thread
        class BitmapDisplayer implements Runnable {
            Bitmap bitmap;
            PhotoToLoad photoToLoad;
            ImageAnimation animation;
            Context context;

            public BitmapDisplayer(Bitmap b, PhotoToLoad p, ImageAnimation a) {
                bitmap = b;
                photoToLoad = p;
                animation = a;
                if( p.imageView.getContext() !=null)
                    context = p.imageView.getContext();
            }

            public void run() {
                if (imageViewReused(photoToLoad))
                    return;
                if(bitmap != null && photoToLoad.imageView != null)
                    Core.getInstance(context).imageListener.success(photoToLoad.url);
                else
                    Core.getInstance(context).imageListener.fail(photoToLoad.url);
            }
        }

    }

    public static class FileCache {

        private File cacheDir;

        public FileCache(Context Context) {
            //Find the dir to save cached images
            cacheDir = new File(Context.getCacheDir(), CACHE_DIR_NAME);
            if (!cacheDir.exists()) {
                try {
                    cacheDir.mkdirs();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        public File getFile(String url) {
            String filename = String.valueOf(url.hashCode());
            File f = new File(cacheDir, filename);
            return f;
        }

        public File[] getFiles() {
            if (cacheDir.listFiles() == null || cacheDir.listFiles().length <= 0) return null;
            return cacheDir.listFiles();
        }

        public void clear(File[] files) {
            if (files == null)
                return;
            for (File f : files){
                if(f.exists())
                    f.delete();
            }

        }
    }

    public static class MemoryCache {

        private Map<String, Bitmap> cache = Collections.synchronizedMap(
                new LinkedHashMap<String, Bitmap>(20, 1.5f, true));//Last argument true for LRU ordering
        private long size = 0;//current allocated size
        private long limit = 1000000;//max memory in bytes


        public MemoryCache() {
            //use 25% of available heap size
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP) {
                setLimit(Runtime.getRuntime().maxMemory() / 4);
            } else {
                setLimit(Runtime.getRuntime().maxMemory() / 8);
            }
        }

        public void setLimit(long new_limit) {
            limit = new_limit;
        }

        public Bitmap get(String id) {
            try {
                if (!cache.containsKey(id))
                    return null;
                return cache.get(id);
            } catch (NullPointerException ex) {
                ex.printStackTrace();
                return null;
            }
        }

        public void put(String id, Bitmap bitmap) {
            try {
                if (cache.containsKey(id))
                    size -= getSizeInBytes(cache.get(id));
                cache.put(id, bitmap);
                size += getSizeInBytes(bitmap);
                checkSize();
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }

        private void checkSize() {
            if (size > limit) {
                Iterator<Entry<String, Bitmap>> iter = cache.entrySet().iterator(); //least recently accessed item will be the first one iterated
                while (iter.hasNext()) {
                    Entry<String, Bitmap> entry = iter.next();
                    size -= getSizeInBytes(entry.getValue());
                    iter.remove();
                    if (size <= limit)
                        break;
                }
            }
        }

        public void clear() {
            try {
                if (cache == null) return;
                Iterator<Entry<String, Bitmap>> it = cache.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<String, Bitmap> pair = it.next();
                    pair.setValue(null);
                }
                cache.clear();
                size = 0;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long getSizeInBytes(Bitmap bitmap) {
            if (bitmap == null)
                return 0;
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }

    private static int CopyStream(InputStream is, OutputStream os) {
        int total_count = 0;
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                total_count += count;
                os.write(bytes, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total_count;
    }


    private interface ImageListener {
        void success(String url);
        void fail(String url);
    }


}
