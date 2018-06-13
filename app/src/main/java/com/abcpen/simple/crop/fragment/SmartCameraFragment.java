package com.abcpen.simple.crop.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.abcpen.camera.sdk.CameraView;
import com.abcpen.camera.sdk.call.CameraCropListen;
import com.abcpen.camera.sdk.call.CameraTakePhotoLister;
import com.abcpen.camera.sdk.call.OnPreviewStatusChangeListen;
import com.abcpen.simple.R;
import com.abcpen.simple.SmartCameraCropActivity;
import com.abcpen.simple.TakePhotoGuider;
import com.abcpen.simple.util.Util;
import com.abcpen.simple.view.RotateImageView;

import java.io.File;


/**
 * 拍照功能页面
 * Created by zhaocheng on 15/9/29.
 */
public class SmartCameraFragment extends Fragment implements View.OnClickListener, CameraTakePhotoLister, OnPreviewStatusChangeListen {

    //view
    private CameraView mCameraView;
    private RotateImageView mFlashView, mTakeImageView, mAlbumImageView, mGuiderImageView, mTakeCancel;
    private ProgressDialog progressDialog = null;
    //data
    private int triggerFlashMode = 0;
    private ImageView mTiShiIv;
    private Uri saveUri;
    private String cacheDir;
    private CameraCropListen cameraCropListen;
    private int formType;

    //orientation
    private int current_orientation = 0;
    private OrientationEventListener orientationEventListener = null;

    public void setCameraCropListen(CameraCropListen cameraCropListen) {
        this.cameraCropListen = cameraCropListen;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_smart_camera, null);
        init(view);
        return view;
    }

    public void setFromType(int formType) {
        this.formType = formType;
    }

    private void init(View view) {
        mCameraView = (CameraView) view.findViewById(R.id.camera);
        mCameraView.initCamera(getActivity(), null, this);
        mCameraView.setCameraResultListener(this);


        mTakeImageView = (RotateImageView) view.findViewById(R.id.iv_take_photo_democf);
        mTakeImageView.setEnabled(false);

        mFlashView = (RotateImageView) view.findViewById(R.id.flash_toggle_iv);
        mTiShiIv = (ImageView) view.findViewById(R.id.tishiiv);
        mGuiderImageView = (RotateImageView) view.findViewById(R.id.guideriv);
        mTakeCancel = (RotateImageView) view.findViewById(R.id.take_pic_cancel);
        mAlbumImageView = (RotateImageView) view.findViewById(R.id.open_album_iv);

        mTakeImageView.setOnClickListener(this);
        mFlashView.setOnClickListener(this);
        mGuiderImageView.setOnClickListener(this);
        mTakeCancel.setOnClickListener(this);
        mAlbumImageView.setOnClickListener(this);

        Message msg1 = new Message();
        msg1.what = 1;
        handler.sendMessageDelayed(msg1, 1000);

        changUIForFromType();

        initSaveUri();

        orientationEventListener = new OrientationEventListener(getActivity()) {
            @Override
            public void onOrientationChanged(int orientation) {
                SmartCameraFragment.this.onOrientationChanged(orientation);
            }
        };

        layoutUI();
    }

    private void changUIForFromType() {
        mTakeImageView.setImageResource(R.drawable.selector_tea_camera_vertical);
        mAlbumImageView.setImageResource(R.drawable.ic_album_nor);
        mTakeCancel.setImageResource(R.drawable.ic_cancle_nor);

    }


    private void onOrientationChanged(int orientation) {

        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN)
            return;
        int diff = Math.abs(orientation - current_orientation);
        if (diff > 180)
            diff = 360 - diff;
        if (diff > 60) {
            orientation = (orientation + 45) / 90 * 90;
            orientation = orientation % 360;
            if (orientation != current_orientation) {
                this.current_orientation = orientation;
                layoutUI();
            }
        }
    }

    private void layoutUI() {
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int relative_orientation = (current_orientation + degrees) % 360;
        int ui_rotation = (360 - relative_orientation) % 360;
        Log.d("zc", "ui_rotation" + ui_rotation);
        mFlashView.setOrientation(-ui_rotation, true);
        mTakeImageView.setOrientation(-ui_rotation, true);
        mAlbumImageView.setOrientation(-ui_rotation, true);
        mTakeCancel.setOrientation(-ui_rotation, true);
        mGuiderImageView.setOrientation(-ui_rotation, true);
        mCameraView.setUIRotation(ui_rotation);
    }


    private void initSaveUri() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            cacheDir = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/Android/data/" + getActivity().getPackageName() + "/paiti/image/";
            File file = new File(cacheDir);
            if (!file.exists()) file.mkdirs();
        } else {
            // 内部存储
            cacheDir = getActivity().getFilesDir().getAbsolutePath() + "/";
        }
        cacheDir += System.currentTimeMillis();
        saveUri = Uri.parse("file://" + cacheDir);
        mCameraView.setSavePhotoUri(saveUri);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("zc", "cameraFragment onResume");
        mCameraView.onResume();
        orientationEventListener.enable();
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            mCameraView.onPause();
        } else {
            mCameraView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("zc", "cameraFragment onPause");
        mCameraView.onPause();
        orientationEventListener.disable();
    }

    /**
     * 相册
     */
    private void openAlbum() {
        Intent openAlbumIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        getActivity().startActivityForResult(openAlbumIntent,
                SmartCameraCropActivity.ALBUM_REQUEST_CODE);
        getActivity().overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);

    }

    /**
     * 帮助
     */
    private void openHelp() {
        startActivity(TakePhotoGuider.getIntent(getActivity()));
    }

    /**
     * 取消拍照
     */
    private void cancelTakePhoto() {
        getActivity().finish();
    }

    /**
     * 切换闪光灯
     */
    private void changeFlash() {
        triggerFlashMode = ++triggerFlashMode % 3;
        int drawable = R.drawable.ic_flash_auto;
        if (triggerFlashMode == 0) {
            drawable = R.drawable.ic_flash_off;
            mCameraView.setFlash(0);
        } else if (triggerFlashMode == 1) {
            drawable = R.drawable.ic_flash_on;
            mCameraView.setFlash(1);
        } else if (triggerFlashMode == 2) {
            drawable = R.drawable.ic_flash_auto;
            mCameraView.setFlash(2);
        }
        mFlashView.setImageResource(drawable);
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
        }
        progressDialog.setMessage("图片处理中...");
        progressDialog.show();

        mCameraView.takePhoto();
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (getActivity() == null || getActivity().isFinishing()) {
                return;
            }
            animImageView(msg);
        }
    };


    Animation.AnimationListener animationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            Message msg2 = new Message();
            msg2.what = 2;
            handler.sendMessageDelayed(msg2, 1000);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private void animImageView(Message msg) {
        switch (msg.what) {

            case 1:
                Animation mAnimation_tishi = AnimationUtils.loadAnimation(
                        getActivity(), R.anim.loading_camera);
                mAnimation_tishi.setAnimationListener(animationListener);
                mAnimation_tishi.setFillAfter(true);
                mTiShiIv.startAnimation(mAnimation_tishi);
                break;

            case 2:
                mTiShiIv.clearAnimation();
                mTiShiIv.setVisibility(View.INVISIBLE);
                break;

        }
    }

    /**
     * 子工程中 不能使用switch(v.getId()) 因 报错问题 转换为 if else...
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (Util.isFastClick()) return;
        if (v.getId() == R.id.iv_take_photo_democf) {

            takePhoto();

        } else if (v.getId() == R.id.flash_toggle_iv) {

            changeFlash();

        } else if (v.getId() == R.id.guideriv) {

            openHelp();

        } else if (v.getId() == R.id.take_pic_cancel) {

            cancelTakePhoto();

        } else if (v.getId() == R.id.open_album_iv) {

            openAlbum();
        }

    }


    @Override
    public void onTakePhotoCompile(Uri uri) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (uri == null) return;
        mCameraView.onPause();
        if (cameraCropListen != null) cameraCropListen.takePhotoSuccess(uri);
    }

    @Override
    public void onTakePhotoFail() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        getActivity().finish();
    }

    @Override
    public void onSurfaceCreated() {


    }

    @Override
    public void onSurfaceDestroyed() {
    }

    @Override
    public void onTryAutoFocus(boolean success) {
        if (success)
            mTakeImageView.setEnabled(true);
        else
            mTakeImageView.setEnabled(false);
    }


}
