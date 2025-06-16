package com.example.inviguard;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

public class ResultActivity extends AppCompatActivity {

    private TextView riskScore;
    private TextView riskText;
    private TextView riskLevel1;
    private TextView riskLevel1Text;
    private TextView ieumMessageText;
    private TextView textResultNotify;
    private LinearLayout riskTypesTexts;

    private Button buttonReport;
    private Button buttonAdditional;
    private Button buttonMain;

    // Intent로 받을 데이터
    private int sessionId;
    private int score;
    private int isHarassment;
    private int shouldReport;
    private int sessionEvalResultId;
    private ArrayList<Integer> categoryIds;
    private ArrayList<Integer> severityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // 뷰 바인딩
        riskScore = findViewById(R.id.risk_score);
        riskText = findViewById(R.id.risk_text);
        riskLevel1 = findViewById(R.id.risk_level1);
        riskLevel1Text = findViewById(R.id.risk_level1_text);
        ieumMessageText = findViewById(R.id.ieum_message_text);
        textResultNotify = findViewById(R.id.text_result_notify);
        riskTypesTexts = findViewById(R.id.risk_types_texts);

        buttonReport = findViewById(R.id.button_report);
        buttonAdditional = findViewById(R.id.button_additional);
        buttonMain = findViewById(R.id.button_main);

        // Intent에서 데이터 받기
        getIntentData();

        // UI 세팅
        setupUI();

        // 버튼 클릭 리스너
        buttonReport.setOnClickListener(v -> showProceedDialog());
        buttonMain.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, MainBeforeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void getIntentData() {
        sessionId = getIntent().getIntExtra("session_id", -1);
        score = getIntent().getIntExtra("risk_score", 0);
        isHarassment = getIntent().getIntExtra("is_harassment", 0);
        shouldReport = getIntent().getIntExtra("should_report", 0);
        sessionEvalResultId = getIntent().getIntExtra("session_eval_result_id", 0);
        categoryIds = getIntent().getIntegerArrayListExtra("category_ids");
        severityList = getIntent().getIntegerArrayListExtra("severity_list");

        if (categoryIds == null) categoryIds = new ArrayList<>();
        if (severityList == null) severityList = new ArrayList<>();
    }

    private void setupUI() {
        // 1. risk_score 설정
        riskScore.setText(score + "점");

        // 텍스트 그라디언트
        riskScore.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                riskScore.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Shader textShader = new LinearGradient(0, 0, 0, riskScore.getHeight(),
                        new int[]{
                                Color.parseColor("#FFC5F1F6"),
                                Color.parseColor("#FFB9F5CE")
                        }, null, Shader.TileMode.CLAMP);
                riskScore.getPaint().setShader(textShader);
            }
        });

        // 2. risk_text, ieum_message_text, text_result_notify 설정
        if (score >= 70) {
            riskText.setText("이건 혼자 참을 일이 아니에요");
            ieumMessageText.setText("지금 느끼는 감정은 절대 혼자 감당할 일이 아니에요. 도움을 요청하는 건 용기 있는 행동이고, 당신이 더 안전하고 편안할 수 있도록 함께할게요. 절대 잘못한 게 아니니 꼭 도움받으세요.");
            textResultNotify.setText("더 이상 혼자 고민하지 마세요, 지금 바로 신고하세요.");
        } else if (score >= 30) {
            riskText.setText("혹시 또 비슷한 일이 있었나요?");
            ieumMessageText.setText("지금 겪고 있는 불편함은 분명 중요한 신호예요. 혹시 비슷한 상황이 더 있었는지, 혹은 더 자세한 증거가 있다면 함께 나눠주세요. 제가 당신의 경험을 존중하고 최선을 다해 도울게요.");
            textResultNotify.setText("비슷한 일이 반복된다면, 증거를 더 제출해 주세요.");
        } else {
            riskText.setText("지나간 말도 마음에 남을 수 있어요");
            ieumMessageText.setText("표면적으로는 큰 문제가 없어 보여도, 마음이 불편했던 건 분명 의미 있는 감정이에요. 만약 불편함이 계속된다면 제게 언제든 이야기해 주세요. 더 자세한 상황이나 증거가 있다면 함께 검토해 볼게요.");
            textResultNotify.setText("언제든 도움이 필요하면 다시 찾아와 주세요.");
        }

        // 3. severity 평균 계산 및 risk_level1 설정
        double avgSeverity = calculateAverageSeverity();
        if (avgSeverity >= 1.0) {
            riskLevel1.setText("🔴 명백한 괴롭힘");
        } else if (avgSeverity >= 0.0) {
            riskLevel1.setText("🟠 불쾌한 괴롭힘");
        } else {
            riskLevel1.setText("🟢 일반적 괴롭힘");
        }

        // 4. risk_level1_text 설정
        riskLevel1Text.setText("·  AI 분석 점수 : " + score + "점");

        // 5. 괴롭힘 유형 설정
        setupHarassmentTypes();
    }

    private double calculateAverageSeverity() {
        if (severityList.isEmpty()) return 0.0;

        int sum = 0;
        for (int severity : severityList) {
            sum += severity;
        }
        return (double) sum / severityList.size();
    }

    private void setupHarassmentTypes() {
        // 기존 뷰들 제거
        riskTypesTexts.removeAllViews();

        for (int categoryId : categoryIds) {
            TextView typeText = new TextView(this);
            typeText.setText("·  " + getHarassmentTypeDescription(categoryId));
            typeText.setTextSize(16);
            typeText.setTextColor(getResources().getColor(R.color.text_primary));

            // 텍스트 간격 설정
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.topMargin = (int) (10 * getResources().getDisplayMetrics().density); // 10dp를 px로 변환
            typeText.setLayoutParams(params);

            riskTypesTexts.addView(typeText);
        }
    }

    private String getHarassmentTypeDescription(int categoryId) {
        switch (categoryId) {
            case 1: return "경멸, 비난, 조롱 표현";
            case 2: return "특정 집단에 대한 혐오";
            case 3: return "차별 정당화 또는 편견 표현";
            case 4: return "성적 대상화, 성희롱, 음란 표현";
            case 5: return "물리적·심리적 위협, 폭력성";
            case 6: return "욕설, 모욕, 감정적 폭력";
            case 7: return "범죄 행위의 조장 또는 정당화";
            case 8: return "비하적 언어, 인신공격";
            case 9: return "과도한 업무 부과";
            case 10: return "부당한 업무 지시, 원래 업무 외 지시";
            case 11: return "부당한 업무 배제, 따돌림";
            case 12: return "고의적 업무 방해";
            case 13: return "불합리한 인사조치";
            case 14: return "회식비 강요 등 경제적 부담";
            case 15: return "직장 내 고립, 따돌림";
            default: return "알 수 없는 유형";
        }
    }

    private void showProceedDialog() {
        // dp → px 변환
        float d = getResources().getDisplayMetrics().density;
        int widthPx = (int) (300 * d);
        int heightPx = (int) (250 * d);

        // Dialog 생성
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_proceed_report);
        dialog.setCancelable(true);

        // 내부 뷰 바인딩 & 리스너
        dialog.findViewById(R.id.iv_close).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, SituationActivity.class);
            startActivity(intent);
            finish();
        });

        // 윈도우 크기·배경·dim 설정
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(widthPx, heightPx);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.dimAmount = 0.8f;
            window.setAttributes(lp);
        }

        dialog.show();
    }
}