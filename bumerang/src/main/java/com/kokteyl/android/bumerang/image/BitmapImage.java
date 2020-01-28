package com.kokteyl.android.bumerang.image;

import android.content.Context;
import android.graphics.Bitmap;

public class BitmapImage implements BumerangImage {
    private Bitmap bitmap;

    public BitmapImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap(Context context) {
        return bitmap;
    }
}