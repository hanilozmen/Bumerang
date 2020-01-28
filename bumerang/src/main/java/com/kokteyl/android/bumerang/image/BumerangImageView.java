package com.kokteyl.android.bumerang.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.lang.reflect.Method;

public class BumerangImageView extends ImageView {
    private BumerangImageTask currentTask;
    private static Object admostImageLoader;
    private static final String LOGTAG = "QUMPARA_OFFERWALL_IMAGE";

    private static final Object admostImageLoaderLock = new Object();

    public BumerangImageView(Context context) {
        super(context);
    }

    public BumerangImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BumerangImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // Helpers to set image by URL
    public void setImageUrl(String url) {
        //TODO
        /*if(isAdMostAvailable())
            loadImageViaAdMost(url, this, null);
        else*/
        BumerangImageLoader.Core.getInstance(this.getContext()).DisplayImage(url, this, false);
    }

    public void setImageUrl(String url, BumerangImageTask.OnCompleteListener completeListener) {
        setImage(new BumerangWebImage(url), completeListener);
    }

    public void setImageUrl(String url, final Integer fallbackResource) {
        //TODO
        /*if(isAdMostAvailable())
            loadImageViaAdMost(url, this, fallbackResource);
        else*/
        BumerangImageLoader.Core.getInstance(this.getContext()).DisplayImage(url, this, false);

    }

    public void setImageUrl(String url, final Integer fallbackResource, BumerangImageTask.OnCompleteListener completeListener) {
        setImage(new BumerangWebImage(url), fallbackResource, completeListener);
    }

    public void setImageUrl(String url, final Integer fallbackResource, final Integer loadingResource) {
        setImage(new BumerangWebImage(url), fallbackResource, loadingResource);
    }

    public void setImageUrl(String url, final Integer fallbackResource, final Integer loadingResource, BumerangImageTask.OnCompleteListener completeListener) {
        setImage(new BumerangWebImage(url), fallbackResource, loadingResource, completeListener);
    }

    // Set image using BumerangImage object
    public void setImage(final BumerangImage image) {
        setImage(image, null, null, null);
    }

    public void setImage(final BumerangImage image, final BumerangImageTask.OnCompleteListener completeListener) {
        setImage(image, null, null, completeListener);
    }

    public void setImage(final BumerangImage image, final Integer fallbackResource) {
        setImage(image, fallbackResource, null, null);
    }

    public void setImage(final BumerangImage image, final Integer fallbackResource, BumerangImageTask.OnCompleteListener completeListener) {
        setImage(image, fallbackResource, null, completeListener);
    }

    public void setImage(final BumerangImage image, final Integer fallbackResource, final Integer loadingResource) {
        setImage(image, fallbackResource, null, null);
    }

    public void setImage(final BumerangImage image, final Integer fallbackResource, final Integer loadingResource, final BumerangImageTask.OnCompleteListener completeListener) {
        // Set a loading resource
        if(loadingResource != null){
            setImageResource(loadingResource);
        }

        // Cancel any existing tasks for this image view
        if(currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }

        // Set up the new task
        currentTask = new BumerangImageTask(getContext(), image);
        currentTask.setOnCompleteHandler(new BumerangImageTask.OnCompleteHandler() {
            @Override
            public void onComplete(Bitmap bitmap) {
                if(bitmap != null) {
                    setImageBitmap(bitmap);
                } else {
                    // Set fallback resource
                    if(fallbackResource != null) {
                        setImageResource(fallbackResource);
                    }
                }

                if(completeListener != null){
                    completeListener.onComplete(bitmap);
                }
            }
        });

    }

    public static void cancelAllTasks(Context context) {
        try {
           BumerangImageLoader.Core.getInstance(context).clear();
        }catch (Exception e) {
        }
    }

    public static boolean isAdMostAvailable() {
        try {
             Class.forName("admost.sdk.base.AdmostImageLoader");
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    public static Object getAdMostImageLoader() {
        try {
            Class c = Class.forName("admost.sdk.base.AdmostImageLoader");
            Method meth = c.getMethod("getInstance");
            return meth.invoke(null);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static boolean loadImageViaAdMost(String url, ImageView imageView, Integer fallbackResource) {
        try {
            synchronized (admostImageLoaderLock) {
                if(admostImageLoader == null) {
                    Log.i(LOGTAG, "Admost is available and AdMostImageLoader will be used to load images!");
                    admostImageLoader = getAdMostImageLoader();
                }
            }
            Class c = Class.forName("admost.sdk.base.AdmostImageLoader");
            Method mtd = c.getMethod("loadImage", String.class, ImageView.class);
            mtd.invoke(admostImageLoader, url, imageView);
            return true;
        }catch (Exception e) {
            return false;
        }

    }
}