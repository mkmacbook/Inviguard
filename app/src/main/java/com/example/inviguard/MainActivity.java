package com.example.inviguard;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView bubble1, bubble2, bubble3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 채팅 바로가기 누르면 챗봇으로 이동
        Button goButton = findViewById(R.id.button_go);
        goButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        bubble1 = findViewById(R.id.bubble_button_1);
        bubble2 = findViewById(R.id.bubble_button_2);
        bubble3 = findViewById(R.id.bubble_button_3);

        bubble1.setOnClickListener(v -> showPopup(
                "Q. 어떤 증거가 필요할까?",
                "A. 괴롭힘의 증거가 될 사진이나 음성녹음\n파일이 있다면 더 정확한 분석이 가능해요."
        ));

        bubble2.setOnClickListener(v -> showPopup(
                "Q. 익명보장이 되나요?",
                "A. 인비가드는 이름 없이도 신고할 수 있으며,\n모든 정보는 안전하게 처리돼요."
        ));

        bubble3.setOnClickListener(v -> showPopup(
                "Q. 신고는 어떻게 진행되나요?",
                "A. 챗봇 상담 후, 제출된 증거를 기반으로\n처리 절차가 안내되고 접수가 진행돼요."
        ));
    }

    private void showPopup(String questionText, String answerText) {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup); // 팝업 레이아웃

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvQuestion = dialog.findViewById(R.id.popup_title);
        TextView tvAnswer = dialog.findViewById(R.id.popup_subtitle);
        ImageView btnClose = dialog.findViewById(R.id.iv_close);

        tvQuestion.setText(questionText);
        tvAnswer.setText(answerText);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}