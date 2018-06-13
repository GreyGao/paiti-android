package com.abcpen.simple.crop.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.abcpen.answer.ABCImageProcessingUtil;
import com.abcpen.simple.R;
import com.abcpen.simple.RotateBitmap;
import com.abcpen.simple.SmartCameraCropActivity;
import com.abcpen.simple.crop.old.CropImageView;
import com.abcpen.simple.crop.old.CropMo;
import com.abcpen.simple.crop.old.CropPresenter;
import com.abcpen.simple.crop.old.HighlightView;
import com.abcpen.simple.crop.old.ICropView;
import com.abcpen.simple.util.BitmapRegionLoader;
import com.abcpen.simple.util.FileCachePathUtil;
import com.abcpen.simple.util.Util;
import com.abcpen.simple.view.RotateImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * SmartCropFragment
 * 裁剪功能页面
 * Created by zhaocheng on 15/9/29.
 * milestone_m7
 */
public class SmartCropFragment extends Fragment implements View.OnClickListener, ICropView {
    private static final String TAG = SmartCropFragment.class.getSimpleName();
    private static final int MAX_IMAGE_SIZE = 2000;

    private ProgressDialog progressDialog = null;
    private CropImageView mCropImageView;
    //结果图
    private Bitmap mCropResultBitmap;
    //缩略图
    private Bitmap mThumbBitmap;
    // 处理异步任务
    private CropPresenter mCropPresenter;
    //裁剪图片参数
    private CropMo mCropMo;

    public HighlightView mCrop;

    // 区间读取图片
    public BitmapRegionLoader decoder;

    private RotateImageView mAnswerView;

    private boolean isAngle = false;

    private int oriThumbImgWidth;
    private int oriThumbImgHeight;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_smart_crop, null);
        init(view);
        return view;
    }


    private void init(View view) {
        mCropPresenter = new CropPresenter(getActivity(), this);
        mCropImageView = view.findViewById(R.id.img_crop);
        mAnswerView = view.findViewById(R.id.crop_image_find_answer);

        mAnswerView.setImageResource(R.drawable.selector_tea_finish);
        mAnswerView.setOnClickListener(this);
        view.findViewById(R.id.crop_image_rotate).setOnClickListener(this);
        view.findViewById(R.id.crop_image_retake).setOnClickListener(this);
        mCropMo = new CropMo();
        mCropMo.originImagePath = getArguments().getString(SmartCameraCropActivity.PHOTO_URI);
        mThumbBitmap = Util.getBitmapThumbnailForPath(getActivity(), mCropMo.originImagePath);

        if (mThumbBitmap == null) {
            getActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        //图片矫正
        bitmapAngel();
        RotateBitmap rotateBitmap = new RotateBitmap(mThumbBitmap);
        mCropImageView.setImageRotateBitmapResetBase(rotateBitmap, true);
        mCropPresenter.startCrop();
    }


    private void bitmapAngel() {
        //图片文字修正
        mThumbBitmap = ABCImageProcessingUtil.changeAngleFont(mThumbBitmap);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.crop_image_find_answer) {
            blurryBitmap();
        } else if (v.getId() == R.id.crop_image_rotate) {
            rotateBitmap();
        } else if (v.getId() == R.id.crop_image_retake) {
            if (mListener != null) {
                if (mThumbBitmap != null) {
                    mThumbBitmap.recycle();
                    mThumbBitmap = null;
                }
                mListener.onAgainOpenCamera();
            }
        }
    }

    private void blurryBitmap() {
        boolean blurStatus = ABCImageProcessingUtil.getBlurStatus(mThumbBitmap);
        if (!blurStatus) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("提示")
                    .setMessage("照片模糊 是否重新拍照")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mListener != null) {
                                if (mThumbBitmap != null) {
                                    mThumbBitmap.recycle();
                                    mThumbBitmap = null;
                                }
                                mListener.onAgainOpenCamera();
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cropOriginalBitmap();
                        }
                    }).create().show();

        } else {
            cropOriginalBitmap();
        }


    }


    public void cropOriginalBitmap() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
        }
        progressDialog.setMessage("裁剪中...");
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Rect r = mCrop.getCropRect();
                    mCropResultBitmap = Bitmap.createBitmap(mThumbBitmap, r.left,
                            r.top, r.width(), r.height());

                   final String cropPath = saveCropResultBitmap(mCropResultBitmap);

                    if (mCropResultBitmap != mThumbBitmap && mThumbBitmap != null) {
                        mThumbBitmap.recycle();
                        mThumbBitmap = null;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            if (mListener != null)
                                mListener.onCropFinished(mCropResultBitmap,cropPath , mCropMo.originImagePath);
                        }
                    });

                } catch (Exception e) {

                }

            }
        }).start();

    }

    private String saveCropResultBitmap(Bitmap cropResultBitmap) {
        File cacheImageFile = FileCachePathUtil.getCacheImageFile(getActivity(), String.valueOf(System.currentTimeMillis()));
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(cacheImageFile);
            cropResultBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return cacheImageFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            return "";
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                }
            }
        }

    }


    private void rotateBitmap() {
        mCropMo.rotationDegree -= 90;
        if (mCropMo.rotationDegree == -360) {
            mCropMo.rotationDegree = 0;
        }
        mThumbBitmap = Util.btnRotateImage(mThumbBitmap, -90);
        RotateBitmap rotateBitmap = new RotateBitmap(mThumbBitmap);
        mCropImageView.setImageRotateBitmapResetBase(rotateBitmap, true);
        mCropPresenter.startCrop();
    }

    @Override
    public void centerCrop() {
        if (mCropImageView.getScale() == 1F) {
            mCropImageView.center(true, true);
        }
    }

    @Override
    public void setLightView() {
        setUpHighLightView();
    }


    public void setUpHighLightView() {
        if (mThumbBitmap == null || mCropImageView == null) return;
        Matrix mImageMatrix = mCropImageView.getImageMatrix();
        HighlightView hv = new HighlightView(mCropImageView);

        int width = mThumbBitmap.getWidth();
        int height = mThumbBitmap.getHeight();

        Rect imageRect = new Rect(0, 0, width, height);

        int cropWidth = width * 6 / 7;
        int cropHeight = height * 2 / 5;

        int x = (width - cropWidth) / 2;
        int y = (height - cropHeight) / 2;

        RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);

        hv.setup(mImageMatrix, imageRect, cropRect, mCropMo.circleCrop, false);

        mCropImageView.mHighlightViews.clear(); // Thong added for rotate

        mCropImageView.add(hv);

        mCropImageView.invalidate();
        if (mCropImageView.mHighlightViews.size() == 1) {
            mCrop = mCropImageView.mHighlightViews.get(0);
            mCrop.setFocus(true);
        }
        mCropImageView.center(true, true);

        try {
            mCropImageView.centerBasedOnHighlightView(mCrop);
        } catch (Exception e) {/*NO-OP*/
        }
    }


    //interface
    private CropListener mListener;

    public void setCropListener(CropListener listener) {
        mListener = listener;
    }


}
