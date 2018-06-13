package com.abcpen.simple;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.abcpen.camera.sdk.call.CameraCropListen;
import com.abcpen.simple.crop.fragment.CropListener;
import com.abcpen.simple.crop.fragment.SmartCropFragment;


/**
 * 拍照及Crop的集成页面
 * 这张页面用来集成：
 * 1.拍照功能页面(SmartCameraFragment)
 * 2.裁剪功能页面 (SmartCropFragment)
 * <p>
 * Created by justin on 15/9/29.
 */
public class SmartCameraCropActivity extends AppCompatActivity implements CameraCropListen, CropListener {

    //Constant
    public static final int ALBUM_REQUEST_CODE = 1002;
    public static final int REQUEST_CODE_CROP_IMAGE = 1003;

    public static final String PHOTO_URI = "PHOTO_URI";
    public static final String FORM_TYPE = "FORM_TYPE";
    //fragment
    private SmartCameraFragment mCameraFragment;
    private SmartCropFragment mCropFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕不变黑
        setContentView(R.layout.ac_smart);
        init();
    }


    private void init() {
        mCameraFragment = new SmartCameraFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fm_content, mCameraFragment).commitAllowingStateLoss();
        mCameraFragment.setCameraCropListen(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SmartCameraCropActivity.ALBUM_REQUEST_CODE: {
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = data.getData();
                    if (selectedImage == null) { // HM 1SC , HM1SW ()

                        return;
                    }
                    String[] filePathColumn = {MediaStore.MediaColumns.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    String imagePath = null;
                    if (cursor != null) {
                        try {
                            cursor.moveToFirst();
                            int columnIndex = cursor
                                    .getColumnIndex(filePathColumn[0]);
                            imagePath = cursor.getString(columnIndex);
                        } catch (Exception e) {
                            imagePath = selectedImage.getPath();
                        } finally {
                            cursor.close();
                            cursor = null;
                        }
                    } else {
                        imagePath = selectedImage.getPath();
                    }
                    startCropImage(imagePath);
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void takePhotoSuccess(Uri uri) {
        startCropImage(uri.getPath());
    }

    public void startCropImage(String imagePath) {
        Bundle bundle = new Bundle();
        mCropFragment = new SmartCropFragment();
        mCropFragment.setCropListener(this);
        bundle.putString(PHOTO_URI, imagePath);
        mCropFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fm_content, mCropFragment).addToBackStack(null).commitAllowingStateLoss();
    }


    @Override
    public void onCropFinished(Bitmap croppedImage, String cropPath, String orgPath) {
        // TODO: 2018/4/12 save
        saveData(cropPath);
    }

    private void saveData(String cropPath) {
        Intent intent = new Intent();
        intent.putExtra(SmartCameraCropActivity.PHOTO_URI, cropPath);
        setResult(RESULT_OK, intent);
        finish();
    }


    @Override
    public void onAgainOpenCamera() {
        getSupportFragmentManager().popBackStack();
    }
}
