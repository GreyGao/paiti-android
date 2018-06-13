package com.abcpen.simple;

import android.app.Application;

import com.abcpen.answer.ABCPaiTiManager;

/**
 * Created by zhaocheng on 2018/4/12.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ABCPaiTiManager.getInstance().init(this, "5aac840df1664467549b1fba", "F0B732122E7CADAC4D857E2C25050C7C");
    }
}
