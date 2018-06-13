package com.abcpen.simple.crop.update;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import com.abcpen.simple.R;
import com.abcpen.simple.view.RotateImageView;
import com.liveaa.education.LiveAaNative;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class CropImageActivity extends Activity {
    private static final String TAG = CropImageActivity.class.getName();
    private CropImgView cropView;
    private RotateImageView cropBtn;
    private RotateImageView mRotateBtn;
    private Bitmap mCropedBitmap;
    private RotateImageView mCloseBtn;
    private static final int MSG_CHECK_BLUR = 0x201;
    private static final int MSG_BINARIZE = 0x202;
    static final String ASSET_SCHEME = "file:///android_asset/";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_image_activity);
        cropView = (CropImgView) findViewById(R.id.crop_image_view);
        cropBtn = (RotateImageView) findViewById(R.id.crop_image_ok);
        mRotateBtn = (RotateImageView) findViewById(R.id.crop_image_rotate);
        mCloseBtn = (RotateImageView) findViewById(R.id.crop_image_retake);
        //        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.advert);
        //  Bitmap bitmap =getBitmapFromAsset(this, "que.jpg");
        Bitmap bitmap = getBitmap("big_que.jpg", true);
        cropView.setBitmap(bitmap);
        //int orientation=getScreenOrientation();
//		Log.d(TAG, "orientation:"+orientation);
        cropBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCropedBitmap = cropView.cropBitmap();

                if (mCropedBitmap != null) {
                    new CheckBlurTask(mCropedBitmap).start();
                } else {
                    Toast.makeText(getApplicationContext(), "截图不能为空", Toast.LENGTH_SHORT).show();
                    //finish();
                }

            }
        });
        mRotateBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                cropView.rotate();
            }
        });

        mCloseBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public int getScreenOrientation() {
        Display getOrient = getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        if (getOrient.getWidth() == getOrient.getHeight()) {
            orientation = Configuration.ORIENTATION_SQUARE;
        } else {
            if (getOrient.getWidth() < getOrient.getHeight()) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
            } else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

    class CheckBlurTask extends Thread {
        private Bitmap mBtimap;


        public CheckBlurTask(Bitmap bitmap) {
            super();
            this.mBtimap = bitmap;
        }

        @Override
        public void run() {
            boolean blur = LiveAaNative.getBlurStatus(mBtimap);
            Message msg = new Message();
            msg.what = MSG_CHECK_BLUR;
            msg.arg1 = blur ? 1 : 0;
            mTaskHandler.sendMessage(msg);
        }
    }

    class BinarizeTask extends Thread {
        private Bitmap mBtimap;
        private String mOutputDir;

        public BinarizeTask(Bitmap bitmap) {
            super();
            this.mBtimap = bitmap;
            this.mOutputDir = getExternalCacheDir().getAbsolutePath();
        }

        @Override
        public void run() {
            String binPath = LiveAaNative.getImagePath(mBtimap, mOutputDir);
            Log.d(TAG, "bin path:" + binPath);
            Message msg = new Message();
            msg.what = MSG_BINARIZE;
            Bundle bundle = new Bundle();
            bundle.putString("bin_path", binPath == null ? "" : binPath);
            msg.setData(bundle);
            mTaskHandler.sendMessage(msg);
        }
    }

    protected Handler mTaskHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHECK_BLUR:

                    if (mCropedBitmap != null) {
                        BinarizeTask binTask = new BinarizeTask(mCropedBitmap);
                        binTask.start();
                    }
                    break;
                case MSG_BINARIZE:
                    cropView.destroy();
                    //finish();
                    break;
                default:
                    break;
            }
            super.

                    handleMessage(msg);
        }


    };


    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            // handle exception
        }

        return bitmap;
    }

    /**
     * 读取压缩图片（根据可用内存大小）
     */
    private Bitmap getBitmap(String path, boolean isAsset) {
        Uri uri = Uri.fromFile(new File(path));
        InputStream in = null;
        ContentResolver contentResolver = getContentResolver();
        AssetManager assetManager = getAssets();
        try {
            if (isAsset) {
                in = assetManager.open(path);
            } else {
                in = contentResolver.openInputStream(uri);
            }

            // Decode image size
            Options o = new Options();
            o.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int bitmapWidth = o.outWidth;
            int bitmapHeight = o.outHeight;
            Log.v(TAG, "out width x height:" + bitmapWidth + "," + bitmapHeight);
            int scale = 1;
            //			IMAGE_MAX_SIZE = (int) Math
            //					.sqrt((Runtime.getRuntime().maxMemory() - Runtime
            //							.getRuntime().totalMemory()) / 2) / 7;
            int maxSize = 4096;
            if (o.outHeight > maxSize || o.outWidth > maxSize) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(maxSize
                        / (double) Math.max(o.outHeight, o.outWidth))
                        / Math.log(0.5)));
            }
            // scale = calScale(scale, o.outWidth, o.outHeight);
            Options o2 = new Options();
            o2.inSampleSize = scale;
            o2.inPurgeable = true;
            o2.inInputShareable = true;
            if (isAsset) {
                in = assetManager.open(path);
            } else {
                in = contentResolver.openInputStream(uri);
            }
            Log.v(TAG, "decode options:" + o2.inSampleSize);
            Bitmap b = BitmapFactory.decodeStream(in, null, o2);
            Log.v(TAG, "after samplesize bitmap width x height:" + b.getWidth() + "," + b.getHeight());
            in.close();
            return b;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {

        }
        return null;
    }
}