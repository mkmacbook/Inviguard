package com.example.inviguard;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    // 뷰 선언
    private TextView textResultSummary;  //유사한 괴롭힘 사례가 확인되었어요. / 유사한 괴롭힘 사례가 없어요.
    private TextView textResultNotify;  //신고가 이루어진 경우가 많습니다. / 신고가 이루어지기 어렵습니다.
    private Button buttonReport;         //신고 진행하기
    private Button buttonAdditional;     //증거 추가 제출하기
    private Button buttonMain;           //메인으로

    // 더미 데이터 (백엔드 연동 전)
    private boolean hasSimilarCases = true; //true: 유사사례 있음 / false: 없음

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        //뷰 바인딩
        textResultSummary = findViewById(R.id.text_result_summary);
        textResultNotify = findViewById(R.id.text_result_notify);
        buttonReport = findViewById(R.id.button_report);
        buttonAdditional = findViewById(R.id.button_additional);
        buttonMain = findViewById(R.id.button_main);

        //상태에 따른 UI 세팅
        setupUI();

        //“신고 계속하기” 버튼 클릭 시 팝업 띄우기
        buttonReport.setOnClickListener(v -> showProceedDialog());

        //“증거 추가 제출하기” 버튼 클릭 시 챗봇 화면으로
        //나중에 코드 추가하기!!!


        //“메인으로” 버튼 클릭 시 메인 화면으로
        buttonMain.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupUI() {
        if (hasSimilarCases) {
            //유사 사례 있을 때
            textResultSummary.setText("유사한 괴롭힘 사례가 확인되었어요.");
            textResultNotify.setText("이와 유사한 사례에는 신고가 이루어진 경우가 많습니다.\n지금 바로 신고를 진행하는 걸 추천드려요.");

            //API 연결 후 텍스트, 크기 변경하는 것도 나중에 추가!!

            //신고 진행하기 : gradient_button
            buttonReport.setBackgroundResource(R.drawable.gradient_button);
            buttonReport.setTextColor(getResources().getColor(android.R.color.black));

            //증거 추가 제출하기: gradient_stroke_rounded
            buttonAdditional.setBackgroundResource(R.drawable.gradient_stroke_rounded);
            buttonAdditional.setTextColor(getResources().getColor(R.color.text_primary));

        } else {
            //유사 사례 없을 때
            textResultSummary.setText("유사한 괴롭힘 사례가 없어요.");
            textResultNotify.setText("이와 유사한 사례에는 신고가 이루어지기 어렵습니다.\n증거를 추가 제출해 주신다면 이음이 다시 분석해 드릴게요.");

            //API 연결 후 텍스트,크기 변경하는 것도 나중에 추가!!

            //신고 진행하기: gradient_stroke_rounded로 변경
            buttonReport.setBackgroundResource(R.drawable.gradient_stroke_rounded);
            buttonReport.setTextColor(getResources().getColor(R.color.text_primary));

            //증거 추가 제출하기: gradient_button로 변경
            buttonAdditional.setBackgroundResource(R.drawable.gradient_button);
            buttonAdditional.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    /** 신고 계속하기 버튼 클릭 시 팝업 */
    private void showProceedDialog() {
        // dp → px 변환
        float d = getResources().getDisplayMetrics().density;
        int widthPx = (int) (300 * d);
        int heightPx = (int) (250 * d);

        //Dialog 생성
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_proceed_report);
        dialog.setCancelable(true);

        //내부 뷰 바인딩 & 리스너
        View dialogView = dialog.findViewById(R.id.dialog_root);
        dialog.findViewById(R.id.iv_close).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
                    Intent intent = new Intent(ResultActivity.this, SituationActivity.class);
                    startActivity(intent);
                    finish();
                });

        //윈도우 크기·배경·dim 설정
        Window window = dialog.getWindow();
        if (window != null) {
            //배경 투명
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            //고정 크기 지정
            window.setLayout(widthPx, heightPx);
            //뒤쪽을 #000000 80% 어둡게
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.dimAmount = 0.8f;
            window.setAttributes(lp);
        }

        //다이얼로그 표시
        dialog.show();
    }
}