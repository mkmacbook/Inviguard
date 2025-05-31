package com.example.inviguard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainBeforeActivity extends AppCompatActivity {
    private TextView bubble1, bubble2, bubble3;
    private DrawerLayout drawerLayout;
    private ImageView menuButton;
    private TextView menuClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_before);
        initViews();
        setupExistingFeatures();
        setupMenu();
    }

    // UI 초기화
    private void initViews() {
        bubble1 = findViewById(R.id.bubble_button_1);
        bubble2 = findViewById(R.id.bubble_button_2);
        bubble3 = findViewById(R.id.bubble_button_3);
        drawerLayout = findViewById(R.id.drawer_layout);
        menuButton = findViewById(R.id.menu);
        menuClose = findViewById(R.id.menu_close);
    }

    // 팝업 기능 설정
    private void setupExistingFeatures() {
        // 채팅 바로가기 버튼
        Button goButton = findViewById(R.id.button_go);
        goButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainBeforeActivity.this, ChatActivity.class);
            startActivity(intent);
        });

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
        Dialog dialog = new Dialog(MainBeforeActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvQuestion = dialog.findViewById(R.id.popup_title);
        TextView tvAnswer = dialog.findViewById(R.id.popup_subtitle);
        ImageView btnClose = dialog.findViewById(R.id.iv_close);

        tvQuestion.setText(questionText);
        tvAnswer.setText(answerText);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // 메뉴 설정
    private void setupMenu() {
        // 메뉴 버튼 클릭하면
        menuButton.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });
        // 메뉴 닫기 누르면
        menuClose.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // 오버레이 효과
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                float alpha = 1.0f - (slideOffset * 0.2f);
                View mainContent = drawerLayout.getChildAt(0);
                mainContent.setAlpha(alpha);
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                View mainContent = drawerLayout.getChildAt(0);
                mainContent.setAlpha(0.8f);
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                View mainContent = drawerLayout.getChildAt(0);
                mainContent.setAlpha(1.0f);
            }
            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        drawerLayout.setScrimColor(Color.parseColor("#80000000"));
    }
}