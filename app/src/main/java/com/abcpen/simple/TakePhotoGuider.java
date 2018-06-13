package com.abcpen.simple;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;


public class TakePhotoGuider extends Activity implements OnClickListener {


    private ImageView mPhoto;


    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, TakePhotoGuider.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.ac_guider);
        mPhoto = (ImageView) this.findViewById(R.id.photo);
        mPhoto.setImageResource(R.drawable.bg_take_photo_guider);
        mPhoto.setOnClickListener(this);
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View arg0) {
        finish();
    }

}