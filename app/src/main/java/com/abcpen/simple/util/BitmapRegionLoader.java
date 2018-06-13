package com.abcpen.simple.util;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;

import java.io.IOException;
import java.io.InputStream;

/**
 * Author: zhaocheng
 * Date: 2015-10-26
 * Time: 18:52
 * Name:BitmapRegionLoader
 * Introduction:
 */
public class BitmapRegionLoader {
    private boolean mValid = false;
    private int mRotation = 0;
    private int mOriginalWidth;
    private int mOriginalHeight;
    private Rect mTempRect = new Rect();
    private InputStream mInputStream;
    private volatile BitmapRegionDecoder mBitmapRegionDecoder;
    private Matrix mRotateMatrix;

    public static BitmapRegionLoader newInstance(InputStream in) throws IOException {
        return newInstance(in, 0);
    }

    public static BitmapRegionLoader newInstance(InputStream in, int rotation) throws IOException {
        if (in == null) {
            return null;
        }

        BitmapRegionLoader loader = new BitmapRegionLoader(in);
        if (loader.mValid) {
            loader.mRotation = rotation;
            if (loader.mRotation != 0) {
                loader.mRotateMatrix = new Matrix();
                loader.mRotateMatrix.postRotate(rotation);
            }
            return loader;
        }

        return null;
    }

    private BitmapRegionLoader(InputStream in) throws IOException {
        mInputStream = in;
        mBitmapRegionDecoder = BitmapRegionDecoder.newInstance(in, false);
        if (mBitmapRegionDecoder != null) {
            mOriginalWidth = mBitmapRegionDecoder.getWidth();
            mOriginalHeight = mBitmapRegionDecoder.getHeight();
            mValid = true;
        }
    }


    public synchronized Bitmap decodeRegion(Rect rect, BitmapFactory.Options options) {
        int unsampledInBitmapWidth = -1;
        int unsampledInBitmapHeight = -1;
        int sampleSize = Math.max(1, options != null ? options.inSampleSize : 1);
        if (options != null && options.inBitmap != null) {
            unsampledInBitmapWidth = options.inBitmap.getWidth() * sampleSize;
            unsampledInBitmapHeight = options.inBitmap.getHeight() * sampleSize;
        }

        switch (mRotation) {
            case 90:
            case -270:
                mTempRect.set(
                        rect.top, mOriginalHeight - rect.right,
                        rect.bottom, mOriginalHeight - rect.left);
                break;

            case 180:
            case -180:
                mTempRect.set(
                        mOriginalWidth - rect.right, mOriginalHeight - rect.bottom,
                        mOriginalWidth - rect.left, mOriginalHeight - rect.top);
                break;

            case 270:
            case -90:
                mTempRect.set(
                        mOriginalWidth - rect.bottom, rect.left,
                        mOriginalWidth - rect.top, rect.right);
                break;

            default:
                mTempRect.set(rect);
        }

        Bitmap bitmap = mBitmapRegionDecoder.decodeRegion(mTempRect, options);
        if (bitmap == null) {
            return null;
        }

        if (options != null && options.inBitmap != null &&
                ((mTempRect.width() != unsampledInBitmapWidth
                        || mTempRect.height() != unsampledInBitmapHeight))) {
            Bitmap subBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0,
                    mTempRect.width() / sampleSize,
                    mTempRect.height() / sampleSize);
            if (bitmap != options.inBitmap && bitmap != subBitmap) {
                bitmap.recycle();
            }
            bitmap = subBitmap;
        }

        if (mRotateMatrix != null) {
            Bitmap rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(),
                    mRotateMatrix, true);
            if ((options == null || bitmap != options.inBitmap) && bitmap != rotatedBitmap) {
                bitmap.recycle();
            }
            bitmap = rotatedBitmap;
        }

        return bitmap;
    }

    public synchronized int getWidth() {
        return (mRotation == 90 || mRotation == 270) ? mOriginalHeight : mOriginalWidth;
    }

    public synchronized int getHeight() {
        return (mRotation == 90 || mRotation == 270) ? mOriginalWidth : mOriginalHeight;
    }

    public synchronized void destroy() {
        mBitmapRegionDecoder.recycle();
        mBitmapRegionDecoder = null;
        try {
            mInputStream.close();
        } catch (IOException ignored) {
        }
    }
}
