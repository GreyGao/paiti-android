package com.abcpen.simple.crop.old;

import android.content.Context;
import android.os.Handler;

import java.util.concurrent.CountDownLatch;

/**
 * Author: zhaocheng
 * Date: 2015-08-28
 * Time: 10:56
 * Name:CropPresenter
 * Introduction:处理cropFragmentNew中所有异步任务
 */
public class CropPresenter {

    private Context mContext;
    private ICropView mICropView;

    public CropPresenter(Context context, ICropView cropView) {
        mICropView = cropView;
        mContext = context;
    }

    /**
     * 开始裁剪
     */
    public void startCrop() {
        new MyThread().start();
    }


    class MyThread extends Thread {
        final CountDownLatch latch = new CountDownLatch(1);
        Handler mHandler = new Handler();

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mICropView.centerCrop();
                    latch.countDown();
                }
            });
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mICropView.setLightView();
                }
            });
            super.run();
        }
    }


}
