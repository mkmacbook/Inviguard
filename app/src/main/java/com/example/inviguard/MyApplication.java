package com.example.inviguard;

import android.app.Application;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

// 폰트 크기 반응형을 위한 java 파일
// 휴대폰 세로 크기에 따라 비율에 맞춰 폰트 사이즈 줄어들게
public class MyApplication extends Application {

    private static final float DESIGN_HEIGHT = 800f; //디자인 기준 세로 크기

    @Override
    public void onCreate() {
        super.onCreate();
        adjustFontScale();
    }

    private void adjustFontScale() {
        Configuration configuration = getResources().getConfiguration();
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        float deviceHeight = metrics.heightPixels;
        float scaleFactor = deviceHeight / DESIGN_HEIGHT;

        if (scaleFactor > 1.0f) {
            scaleFactor = 1.0f; //커지는 건 막도록
        }

        configuration.fontScale = scaleFactor;
        getResources().updateConfiguration(configuration, metrics);
    }
}
