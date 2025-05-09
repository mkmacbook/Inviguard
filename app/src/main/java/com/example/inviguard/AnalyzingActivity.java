package com.example.inviguard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class AnalyzingActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyzing);

        //현재 백엔드 연동 전이라 5초 뒤에 분석 결과로 넘어가게 구현
        //서버 연동 후, 서버 분석 완료 여부 받으면 넘어가도록 코드 수정해야 함
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(AnalyzingActivity.this, ResultActivity.class);
                startActivity(intent);
                finish();
            }
        }, 5000);
    }
}