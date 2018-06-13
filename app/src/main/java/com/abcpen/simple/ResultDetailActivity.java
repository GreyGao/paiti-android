package com.abcpen.simple;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;

import com.abcpen.model.AnswerModel;
import com.bumptech.glide.Glide;

import java.util.ArrayList;


/**
 * Author: zhaocheng
 * Date: 2015-11-03
 * Time: 14:26
 * Name:ResultDetailActivity
 * Introduction:
 */
public class ResultDetailActivity extends FragmentActivity {

    private static String INTENT_IMAGE_URL = "intent_image_url";
    private static String INTENT_CONTENT = "intent_content";

    private String mImageUrl;
    private ArrayList<AnswerModel> mContent;
    private ImageView mImageView;
    private ViewPager pager;

    public static void startResultDetailActivity(Context context, String imageUrl, ArrayList<AnswerModel> data) {
        Intent intent = new Intent(context, ResultDetailActivity.class);
        intent.putExtra(INTENT_IMAGE_URL, imageUrl);
        intent.putExtra(INTENT_CONTENT, data);
        context.startActivity(intent);
    }

    public void bindIntent() {
        mImageUrl = getIntent().getStringExtra(INTENT_IMAGE_URL);
        mContent = getIntent().getParcelableArrayListExtra(INTENT_CONTENT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_result_detail);
        bindIntent();
        initView();
    }

    private void initView() {
        mImageView = findViewById(R.id.riv_result);
        pager = findViewById(R.id.view_pager);
        MyPageAdapter adapter = new MyPageAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        Glide.with(this)
                .load(mImageUrl)
                .placeholder(R.color.G2)//图片加载出来前，显示的图片
                .error(R.color.G2)//图片加载失败后，显示的图片
                .into(mImageView);

    }


    class MyPageAdapter extends FragmentPagerAdapter {

        public MyPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ResultFragmentDetail.getInstance(mContent.get(position).question_body, mContent.get(position).quesiton_answer);
        }

        @Override
        public int getCount() {
            return mContent != null ? mContent.size() : 0;
        }
    }


}
