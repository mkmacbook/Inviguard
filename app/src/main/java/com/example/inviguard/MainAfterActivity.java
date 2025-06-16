package com.example.inviguard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

public class MainAfterActivity extends AppCompatActivity implements ChatSessionManager.MainActivityInterface {

    private TextView mainAfterText;
    private View step1Circle, step2Circle, step3Circle, step4Circle;
    private ImageView step1Check, step2Check, step3Check, step4Check;
    private TextView step1Text, step2Text, step3Text, step4Text;
    private View divider1, divider2, divider3;
    private MenuBar menuBar;
    private LinearLayout chatSessionListLayout;
    private ChatSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d("MainAfterActivity", "onCreate 시작");
            setContentView(R.layout.activity_main_after);

            // 모든 UI 요소 찾아서 변수 연결
            initViews();

            // SituationActivity에서 전달받은 정보로 UI 업데이트
            updateUIFromSituationActivity();

            // 메뉴바 (먼저 설정)
            setupMenu();

            // 메뉴바 세션 리스트 (메뉴바 설정 후에)
            setupChatSessionManager();

            // 버튼 리스너 설정
            setupButtonListener();

            Log.d("MainAfterActivity", "onCreate 완료");

        } catch (Exception e) {
            Log.e("MainAfterActivity", "onCreate 중 오류 발생", e);
        }
    }

    private void initViews() {
        try {
            // 메인 텍스트
            mainAfterText = findViewById(R.id.main_after_text);

            // 단계별 원형 뷰들
            step1Circle = findViewById(R.id.step1_circle);
            step2Circle = findViewById(R.id.step2_circle);
            step3Circle = findViewById(R.id.step3_circle);
            step4Circle = findViewById(R.id.step4_circle);

            // 체크 아이콘들
            step1Check = findViewById(R.id.step1_check);
            step2Check = findViewById(R.id.step2_check);
            step3Check = findViewById(R.id.step3_check);
            step4Check = findViewById(R.id.step4_check);

            // 단계별 텍스트들
            step1Text = findViewById(R.id.step1_text);
            step2Text = findViewById(R.id.step2_text);
            step3Text = findViewById(R.id.step3_text);
            step4Text = findViewById(R.id.step4_text);

            // 구분선들
            divider1 = findViewById(R.id.divider1);
            divider2 = findViewById(R.id.divider2);
            divider3 = findViewById(R.id.divider3);

            // 메뉴바 세션 리스트
            chatSessionListLayout = findViewById(R.id.chat_session_list);

            Log.d("MainAfterActivity", "뷰 초기화 완료");

        } catch (Exception e) {
            Log.e("MainAfterActivity", "뷰 초기화 중 오류", e);
        }
    }

    private void updateUIFromSituationActivity() {
        try {
            String reportID = getIntent().getStringExtra("report_id");
            String currentStatus = getIntent().getStringExtra("status");
            int currentStep = getIntent().getIntExtra("current_step", 3);

            // 기본값 설정
            if(reportID == null) reportID = "1번"; // 신고 번호
            if(currentStatus == null) currentStatus = "HR팀 검토 중"; // 진행 단계

            // 메인 텍스트 업데이트
            String fullText = reportID + " 신고건은\n현재 " + currentStatus + "이에요";
            if (mainAfterText != null) {
                mainAfterText.setText(fullText);
            }

            // 현재 단계: 3단계 (HR팀 검토 중)로 고정
            updateStepProgress(3);

            Log.d("MainAfterActivity", "UI 업데이트 완료: " + reportID + ", " + currentStatus);

        } catch (Exception e) {
            Log.e("MainAfterActivity", "UI 업데이트 중 오류", e);
        }
    }

    private void setupButtonListener() {
        try {
            View buttonGo = findViewById(R.id.button_go);
            if (buttonGo != null) {
                buttonGo.setOnClickListener(v -> {
                    try {
                        Log.d("MainAfterActivity", "익명 채팅 버튼 클릭됨");

                        // 중복 클릭 방지
                        buttonGo.setEnabled(false);

                        Intent intent = new Intent(MainAfterActivity.this, ChatActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        startActivity(intent);

                        // 잠시 후 버튼 재활성화
                        buttonGo.postDelayed(() -> buttonGo.setEnabled(true), 1000);

                    } catch (Exception e) {
                        Log.e("MainAfterActivity", "채팅으로 이동 중 오류", e);
                        buttonGo.setEnabled(true);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("MainAfterActivity", "버튼 리스너 설정 중 오류", e);
        }
    }

    // 단계에 따라 UI 업데이트
    private void updateStepProgress(int currentStep) {
        try {
            // 모든 단계를 기본 상태로 초기화
            resetAllSteps();

            // 현재 단계에 따라 UI 상태 변경
            switch (currentStep) {
                case 1:
                    setStepInProgress(1);
                    break;

                case 2:
                    setStepCompleted(1);
                    setDividerActive(divider1, true);
                    setStepInProgress(2);
                    break;

                case 3:
                    setStepCompleted(1);
                    setStepCompleted(2);
                    setDividerActive(divider1, true);
                    setDividerActive(divider2, true);
                    setStepInProgress(3);
                    break;

                case 4:
                    setStepCompleted(1);
                    setStepCompleted(2);
                    setStepCompleted(3);
                    setDividerActive(divider1, true);
                    setDividerActive(divider2, true);
                    setDividerActive(divider3, true);
                    setStepInProgress(4);
                    break;
            }
        } catch (Exception e) {
            Log.e("MainAfterActivity", "단계 업데이트 중 오류", e);
        }
    }

    // 상태 초기화
    private void resetAllSteps() {
        try {
            if (step1Circle != null) step1Circle.setBackgroundResource(R.drawable.ic_circle_stroke_mint);
            if (step2Circle != null) step2Circle.setBackgroundResource(R.drawable.ic_circle_stroke_mint);
            if (step3Circle != null) step3Circle.setBackgroundResource(R.drawable.ic_circle_stroke_mint);
            if (step4Circle != null) step4Circle.setBackgroundResource(R.drawable.ic_circle_stroke_mint);

            if (step1Check != null) step1Check.setVisibility(View.GONE);
            if (step2Check != null) step2Check.setVisibility(View.GONE);
            if (step3Check != null) step3Check.setVisibility(View.GONE);
            if (step4Check != null) step4Check.setVisibility(View.GONE);

            setTextSecondary(step1Text);
            setTextSecondary(step2Text);
            setTextSecondary(step3Text);
            setTextSecondary(step4Text);

            if (divider1 != null) divider1.setBackgroundColor(ContextCompat.getColor(this, R.color.main_mint));
            if (divider2 != null) divider2.setBackgroundColor(ContextCompat.getColor(this, R.color.main_mint));
            if (divider3 != null) divider3.setBackgroundColor(ContextCompat.getColor(this, R.color.main_mint));
        } catch (Exception e) {
            Log.e("MainAfterActivity", "단계 초기화 중 오류", e);
        }
    }

    // 완료 상태 설정
    private void setStepCompleted(int stepNumber) {
        try {
            switch (stepNumber) {
                case 1:
                    if (step1Circle != null) step1Circle.setBackgroundResource(R.drawable.ic_circle_stroke_skyblue);
                    if (step1Check != null) step1Check.setVisibility(View.VISIBLE);
                    setTextSecondary(step1Text);
                    break;
                case 2:
                    if (step2Circle != null) step2Circle.setBackgroundResource(R.drawable.ic_circle_stroke_skyblue);
                    if (step2Check != null) step2Check.setVisibility(View.VISIBLE);
                    setTextSecondary(step2Text);
                    break;
                case 3:
                    if (step3Circle != null) step3Circle.setBackgroundResource(R.drawable.ic_circle_stroke_skyblue);
                    if (step3Check != null) step3Check.setVisibility(View.VISIBLE);
                    setTextSecondary(step3Text);
                    break;
                case 4:
                    if (step4Circle != null) step4Circle.setBackgroundResource(R.drawable.ic_circle_stroke_skyblue);
                    if (step4Check != null) step4Check.setVisibility(View.VISIBLE);
                    setTextSecondary(step4Text);
                    break;
            }
        } catch (Exception e) {
            Log.e("MainAfterActivity", "완료 상태 설정 중 오류", e);
        }
    }

    // 진행 중 상태 설정
    private void setStepInProgress(int stepNumber) {
        try {
            switch (stepNumber) {
                case 1:
                    if (step1Circle != null) step1Circle.setBackgroundResource(R.drawable.ic_circle_filled_mint);
                    if (step1Check != null) step1Check.setVisibility(View.GONE);
                    setTextPrimary(step1Text);
                    break;
                case 2:
                    if (step2Circle != null) step2Circle.setBackgroundResource(R.drawable.ic_circle_filled_mint);
                    if (step2Check != null) step2Check.setVisibility(View.GONE);
                    setTextPrimary(step2Text);
                    break;
                case 3:
                    if (step3Circle != null) step3Circle.setBackgroundResource(R.drawable.ic_circle_filled_mint);
                    if (step3Check != null) step3Check.setVisibility(View.GONE);
                    setTextPrimary(step3Text);
                    break;
                case 4:
                    if (step4Circle != null) step4Circle.setBackgroundResource(R.drawable.ic_circle_filled_mint);
                    if (step4Check != null) step4Check.setVisibility(View.GONE);
                    setTextPrimary(step4Text);
                    break;
            }
        } catch (Exception e) {
            Log.e("MainAfterActivity", "진행 중 상태 설정 중 오류", e);
        }
    }

    // 연결선 색상 설정
    private void setDividerActive(View divider, boolean isSkyblue) {
        try {
            if (divider != null) {
                if (isSkyblue) {
                    divider.setBackgroundColor(ContextCompat.getColor(this, R.color.main_skyblue));
                } else {
                    divider.setBackgroundColor(ContextCompat.getColor(this, R.color.main_mint));
                }
            }
        } catch (Exception e) {
            Log.e("MainAfterActivity", "연결선 색상 설정 중 오류", e);
        }
    }

    // 진행 중인 단계 텍스트 설정
    private void setTextPrimary(TextView textView) {
        try {
            if (textView != null) {
                textView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                textView.setTypeface(ResourcesCompat.getFont(this, R.font.roboto_semibold));
            }
        } catch (Exception e) {
            Log.e("MainAfterActivity", "primary 텍스트 설정 중 오류", e);
        }
    }

    // 완료/대기 단계 텍스트 설정
    private void setTextSecondary(TextView textView) {
        try {
            if (textView != null) {
                textView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                textView.setTypeface(ResourcesCompat.getFont(this, R.font.roboto_medium));
            }
        } catch (Exception e) {
            Log.e("MainAfterActivity", "secondary 텍스트 설정 중 오류", e);
        }
    }

    // 메뉴 설정 - MenuBar 사용 (MainBeforeActivity와 동일한 방식)
    private void setupMenu() {
        try {
            DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
            ImageView menuButton = findViewById(R.id.menu);
            TextView menuClose = findViewById(R.id.menu_close);

            // 필수 요소들 null 체크와 상세 로그
            Log.d("MainAfterActivity", "메뉴 설정 시작");
            Log.d("MainAfterActivity", "drawerLayout: " + (drawerLayout != null ? "존재" : "null"));
            Log.d("MainAfterActivity", "menuButton: " + (menuButton != null ? "존재" : "null"));
            Log.d("MainAfterActivity", "menuClose: " + (menuClose != null ? "존재" : "null"));

            if (drawerLayout != null && menuButton != null && menuClose != null) {
                // MainBeforeActivity와 동일한 방식으로 MenuBar 생성
                menuBar = new MenuBar(drawerLayout, menuButton, menuClose);
                menuBar.setupMenu();

                // 메뉴 열릴 때 세션 목록 갱신을 위한 리스너 설정
                menuBar.setMenuBarListener(new MenuBar.MenuBarListener() {
                    @Override
                    public void onMenuOpened() {
                        Log.d("MainAfterActivity", "메뉴가 열렸습니다");
                        if (sessionManager != null) {
                            sessionManager.fetchSessions();
                        }
                    }
                });

                Log.d("MainAfterActivity", "메뉴바 설정 완료");

            } else {
                Log.e("MainAfterActivity", "메뉴 설정 실패: 필수 UI 요소가 없습니다");
                if (drawerLayout == null) Log.e("MainAfterActivity", "drawer_layout을 찾을 수 없습니다");
                if (menuButton == null) Log.e("MainAfterActivity", "menu 버튼을 찾을 수 없습니다");
                if (menuClose == null) Log.e("MainAfterActivity", "menu_close를 찾을 수 없습니다");
            }
        } catch (Exception e) {
            Log.e("MainAfterActivity", "메뉴 설정 중 오류", e);
        }
    }

    // 채팅 세션 매니저 설정을 별도 메소드로 분리
    private void setupChatSessionManager() {
        try {
            if (chatSessionListLayout != null) {
                sessionManager = new ChatSessionManager(this, chatSessionListLayout);
                sessionManager.fetchSessions();
                Log.d("MainAfterActivity", "채팅 세션 매니저 설정 완료");
            } else {
                Log.w("MainAfterActivity", "chatSessionListLayout이 null입니다");
            }
        } catch (Exception e) {
            Log.e("MainAfterActivity", "채팅 세션 매니저 설정 중 오류", e);
        }
    }

    @Override
    public void runOnMain(Runnable r) {
        runOnUiThread(r);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainAfterActivity", "onResume 호출됨");

        // 메뉴가 제대로 설정되었는지 다시 확인
        if (menuBar == null) {
            Log.w("MainAfterActivity", "menuBar가 null입니다. 다시 설정을 시도합니다.");
            setupMenu();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainAfterActivity", "onPause 호출됨");
    }

    @Override
    public void onBackPressed() {
        // 메뉴가 열려있으면 먼저 메뉴를 닫기
        if (menuBar != null && menuBar.isMenuOpen()) {
            menuBar.closeMenu();
        } else {
            super.onBackPressed();
        }
    }
}