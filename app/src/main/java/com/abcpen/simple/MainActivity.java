package com.abcpen.simple;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.abcpen.answer.ABCPaiTiManager;
import com.abcpen.callback.ABCCallBack;
import com.abcpen.callback.ABCFileCallBack;
import com.abcpen.callback.ABCPaiTiAnswerListener;
import com.abcpen.camera.sdk.util.ACLog;
import com.abcpen.model.PASingleModel;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

/**
 * Created by zhaocheng on 2018/4/12.
 */

public class MainActivity extends AppCompatActivity implements ABCFileCallBack<String>, ABCPaiTiAnswerListener {

    public static final int PHOTO_CODE = 0x001;

    private Button btnOpenCamera, btnAuth;

    private TextView tvUploadProgress;

    private StringBuffer sb;

    public void openCamera(View view) {

        RxPermissions permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            Intent intent = new Intent(MainActivity.this, SmartCameraCropActivity.class);
                            startActivityForResult(intent, PHOTO_CODE);
                        }
                    }
                });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PHOTO_CODE) {
            String filePath = data.getStringExtra(SmartCameraCropActivity.PHOTO_URI);
            ABCPaiTiManager.getInstance().uploadFile(filePath, this);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_main);
        sb = new StringBuffer();
        btnOpenCamera = findViewById(R.id.btn_open_camera);
        tvUploadProgress = findViewById(R.id.tv_upload_progress);
        btnAuth = findViewById(R.id.btn_auth);
        if (ABCPaiTiManager.getInstance().isAuth()) {
            btnOpenCamera.setVisibility(View.VISIBLE);
            btnAuth.setVisibility(View.GONE);
        } else {
            btnOpenCamera.setVisibility(View.GONE);
            btnAuth.setVisibility(View.VISIBLE);
        }
        ABCPaiTiManager.getInstance().registerOnReceiveListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ABCPaiTiManager.getInstance().unRegisterOnReceiveListener(this);
    }

    public void authToSDK(View view) {
        ABCPaiTiManager.getInstance().authToSDK(new ABCCallBack() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(MainActivity.this, "认证成功", Toast.LENGTH_SHORT).show();
                btnOpenCamera.setVisibility(View.VISIBLE);
                ACLog.d("authToSDK success");
            }

            @Override
            public void onFail(Exception e) {
                Toast.makeText(MainActivity.this, "认证失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSuccess(String s) {
        tvUploadProgress.setText(sb.append("\n上传成功 题目正在分析中" + "img_id == " + s).toString());
    }

    @Override
    public void onFail(Exception e) {
        tvUploadProgress.setText(sb.append("\n上传失败").toString());
    }

    @Override
    public void onUploadProgress(int progress) {
        tvUploadProgress.setText(sb.toString() + "===>progress" + progress);
    }

    @Override
    public void onAnswerData(String imgId, PASingleModel paSingleModel) {
        ResultActivity.startResultActivity(
                MainActivity.this, paSingleModel.result.question.image_path, paSingleModel.result.question.image_id,
                paSingleModel.result.answers);
        tvUploadProgress.setText(sb.append("\n识别成功" + imgId).toString());
    }

    @Override
    public void noGetAnswerData(String imgId) {
        tvUploadProgress.setText(sb.append("\n无答案" + imgId).toString());
    }
}
