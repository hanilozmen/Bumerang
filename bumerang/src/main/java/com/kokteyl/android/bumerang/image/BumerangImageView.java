package com.kokteyl.android.bumerang.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BumerangImageView extends ImageView {
    private BumerangImageTask currentTask;
    private static final String LOGTAG = "QUMPARA_OFFERWALL_IMAGE";

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
    public void loadImage(String url) {
        if (getContext() == null) return;
        BumerangImageLoader.Core.getInstance(getContext()).displayImage(this, url);
    }

    /* TODO hata var cozmek lazim */
    private void loadImage(String url, BumerangImageTask.OnCompleteListener completeListener) {
        loadImageInternal(new BumerangWebImage(url), null, null, completeListener);
    }

    private void loadImage(String url, final Integer fallbackResource, BumerangImageTask.OnCompleteListener completeListener) {
        loadImageInternal(new BumerangWebImage(url), fallbackResource, null, completeListener);
    }

    private void loadImage(String url, final Integer fallbackResource, final Integer loadingResource) {
        loadImageInternal(new BumerangWebImage(url), fallbackResource, loadingResource, null);
    }

    private void loadImage(String url, final Integer fallbackResource, final Integer loadingResource, BumerangImageTask.OnCompleteListener completeListener) {
        loadImageInternal(new BumerangWebImage(url), fallbackResource, loadingResource, completeListener);
    }


    private void loadImageInternal(final BumerangImage image, final Integer fallbackResource, final Integer loadingResource, final BumerangImageTask.OnCompleteListener completeListener) {
        // Set a loading resource
        if (loadingResource != null) {
            setImageResource(loadingResource);
        }

        // Cancel any existing tasks for this image view
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }

        // Set up the new task
        currentTask = new BumerangImageTask(getContext(), image);
        currentTask.setOnCompleteHandler(new BumerangImageTask.OnCompleteHandler() {
            @Override
            public void onComplete(Bitmap bitmap) {
                if (bitmap != null) {
                    setImageBitmap(bitmap);
                } else {
                    // Set fallback resource
                    if (fallbackResource != null) {
                        setImageResource(fallbackResource);
                    }
                }

                if (completeListener != null) {
                    completeListener.onComplete(bitmap);
                }
            }
        });

    }

}