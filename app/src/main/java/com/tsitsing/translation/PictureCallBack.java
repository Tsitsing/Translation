package com.tsitsing.translation;

import android.graphics.Bitmap;

public interface PictureCallBack {
    public void doSuccess (Bitmap bitmap);
    public void doFail ();
}
