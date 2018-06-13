package com.abcpen.simple.crop.fragment;

import android.graphics.Bitmap;

/**
 * Author: zhaocheng
 * Date: 2015-11-04
 * Time: 17:52
 * Name:CropListern
 * Introduction:
 */
public interface CropListener {
    void onCropFinished(Bitmap croppedImage, String cropPath, String mImagePath);

    void onAgainOpenCamera();
}
