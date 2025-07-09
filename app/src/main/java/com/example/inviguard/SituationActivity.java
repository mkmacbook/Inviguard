package com.example.inviguard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import okhttp3.*;

public class SituationActivity extends AppCompatActivity {
    private static final String TAG = "SituationActivity";
    private static final String BASE_URL = "http://10.0.2.2:3000/api/chat";

    private Button buttonAdditional;
    private Button buttonMain;
    private TextView textState;
    private TextView textNumber;
    private TextView textDate;
    private TextView textCompletion;
    private ProgressBar progressSteps;

    private OkHttpClient client;
    private int reportId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_situation);

        // 뷰 초기화
        initViews();

        client = new OkHttpClient();

        // 임시로 최근 Report 조회 (실제로는 사용자별 Report 조회 API 필요)
        fetchLatestReport();

        // 버튼 리스너 설정
        setupButtonListeners();
    }

    private void initViews() {
        buttonAdditional = findViewById(R.id.button_additional);
        buttonMain = findViewById(R.id.button_main);
        textState = findViewById(R.id.text_state);
        textNumber = findViewById(R.id.text_number);
        textDate = findViewById(R.id.text_date);
        textCompletion = findViewById(R.id.text_completion);
        progressSteps = findViewById(R.id.progress_steps);

        // ProgressBar 초기화
        progressSteps.setMax(3);
    }

    private void setupButtonListeners() {
        // "메인으로" 버튼 클릭 시 메인 화면으로
        buttonMain.setOnClickListener(v -> {
            try {
                Log.d(TAG, "메인으로 버튼 클릭됨");

                // 중복 클릭 방지
                buttonMain.setEnabled(false);

                Intent intent = new Intent(SituationActivity.this, MainAfterActivity.class);
                intent.putExtra("report_id", reportId + "번");
                intent.putExtra("status", "HR팀 검토 중");
                intent.putExtra("current_step", 3);

                // 플래그 추가로 액티비티 스택 정리
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                Log.d(TAG, "MainAfterActivity로 이동 시작");
                startActivity(intent);
                finish();

            } catch (Exception e) {
                Log.e(TAG, "메인으로 이동 중 오류 발생", e);
                buttonMain.setEnabled(true); // 오류 시 버튼 다시 활성화
            }
        });

        // "증거 추가 제출하기" 버튼
        buttonAdditional.setOnClickListener(v -> {
            try {
                Log.d(TAG, "증거 추가 제출 버튼 클릭됨");

                buttonAdditional.setEnabled(false);

                Intent intent = new Intent(SituationActivity.this, ChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);

            } catch (Exception e) {
                Log.e(TAG, "채팅으로 이동 중 오류 발생", e);
                buttonAdditional.setEnabled(true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 액티비티가 다시 보일 때 버튼 재활성화
        if (buttonMain != null) {
            buttonMain.setEnabled(true);
        }
        if (buttonAdditional != null) {
            buttonAdditional.setEnabled(true);
        }
    }

    private void fetchLatestReport() {
        // 실제로는 사용자별 Report 목록 조회 API가 필요하지만,
        // 현재는 Report ID를 직접 사용 (테스트용)
        // 가장 최근 Report ID를 가져오는 방식으로 구현
        // 임시로 Report ID = 1로 설정
        reportId = 1;
        fetchReportDetails(reportId);
    }

    private void fetchReportDetails(int reportId) {
        updateReportUI(reportId);
    }

    private void updateReportUI(int reportId) {
        try {
            // 현재 날짜/시간
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String submittedDate = dateFormat.format(currentDate);

            // 예상 완료일 (접수일로부터 3일 후)
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            calendar.add(Calendar.DAY_OF_MONTH, 3);
            SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String expectedCompletion = dateOnlyFormat.format(calendar.getTime());

            // UI 업데이트
            runOnUiThread(() -> {
                textState.setText("진행 단계 : HR팀 검토 중");
                textNumber.setText("신고 번호 : " +  reportId + "번");
                textDate.setText("접수 일시 : " + submittedDate);
                textCompletion.setText("예상 완료일 : " + expectedCompletion);

                // 진행 단계 설정 (접수완료(0) → 분석완료(1) → 검토중(2) → 완료(3))
                int currentStep = 2; // 검토 중
                progressSteps.setProgress(currentStep);

                Log.d(TAG, "신고 정보 UI 업데이트 완료 - Report ID: " + reportId);
            });

        } catch (Exception e) {
            Log.e(TAG, "신고 정보 UI 업데이트 실패", e);
            runOnUiThread(() -> {
                // 기본값 설정
                textState.setText("진행 단계 : 확인 중");
                textNumber.setText("신고 번호 : 조회 중...");
                textDate.setText("접수 일시 : 조회 중...");
                textCompletion.setText("예상 완료일 : 조회 중...");
                progressSteps.setProgress(1);
            });
        }
    }
}