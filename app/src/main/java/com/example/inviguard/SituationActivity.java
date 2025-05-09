package com.example.inviguard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;

public class SituationActivity extends AppCompatActivity {
    private Button buttonAdditional;
    private Button buttonMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_situation);

        buttonAdditional = findViewById(R.id.button_additional);
        buttonMain = findViewById(R.id.button_main);

        // ProgressBar 초기화
        ProgressBar progress = findViewById(R.id.progress_steps);
        progress.setMax(3);  // 4단계(0~3)

        int currentStep = 2;
        progress.setProgress(currentStep);

        //“증거 추가 제출하기” 버튼 클릭 시 챗봇 화면으로
        //나중에 코드 추가하기!!!


        //“메인으로” 버튼 클릭 시 메인 화면으로
        buttonMain.setOnClickListener(v -> {
            Intent intent = new Intent(SituationActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
