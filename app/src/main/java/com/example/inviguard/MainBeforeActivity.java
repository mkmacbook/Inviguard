package com.example.inviguard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainBeforeActivity extends AppCompatActivity implements ChatSessionManager.MainActivityInterface{
    private TextView bubble1, bubble2, bubble3;
    private MenuBar menuBar;

    private LinearLayout chatSessionListLayout;
    private ChatSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_before);
        initViews();
        setupExistingFeatures();
        setupMenu();

        sessionManager = new ChatSessionManager(this, chatSessionListLayout);
        sessionManager.fetchSessions();
    }

    // UI 초기화
    private void initViews() {
        bubble1 = findViewById(R.id.bubble_button_1);
        bubble2 = findViewById(R.id.bubble_button_2);
        bubble3 = findViewById(R.id.bubble_button_3);

        chatSessionListLayout = findViewById(R.id.chat_session_list);
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

    // 메뉴 설정 - MenuHelper 사용
    private void setupMenu() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ImageView menuButton = findViewById(R.id.menu);
        TextView menuClose = findViewById(R.id.menu_close);

        menuBar = new MenuBar(drawerLayout, menuButton, menuClose);
        menuBar.setupMenu();

        // 메뉴가 열릴 때마다 세션 목록 새로고침
        menuBar.setMenuBarListener(() -> sessionManager.fetchSessions());
    }

    // ChatSessionManager → runOnMain 실행 인터페이스 구현
    @Override
    public void runOnMain(Runnable r) {
        runOnUiThread(r);
    }
}