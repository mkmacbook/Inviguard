package com.example.inviguard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import com.google.gson.annotations.SerializedName;
import android.widget.Toast;
import android.util.Log;

public class MainAfterActivity extends AppCompatActivity implements ChatSessionManager.MainActivityInterface {

    private TextView mainAfterText;
    private View step1Circle, step2Circle, step3Circle, step4Circle;
    private ImageView step1Check, step2Check, step3Check, step4Check;  // step3Check, step4Check 추가
    private TextView step1Text, step2Text, step3Text, step4Text;
    private View divider1, divider2, divider3;
    private MenuBar menuBar;
    private LinearLayout chatSessionListLayout;
    private ChatSessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_after);
        // 모든 UI 요소 찾아서 변수 연결
        initViews();
        // API 호출하여 상태 업데이트
        fetchStatusFromAPI();
        // 메뉴바
        setupMenu();
        // 메뉴바 세션 리스트
        sessionManager = new ChatSessionManager(this, chatSessionListLayout);
        sessionManager.fetchSessions();
    }

    private void initViews() {
        // 메인 텍스트
        mainAfterText = findViewById(R.id.main_after_text);

        // 단계별 원형 뷰들
        step1Circle = findViewById(R.id.step1_circle);
        step2Circle = findViewById(R.id.step2_circle);
        step3Circle = findViewById(R.id.step3_circle);
        step4Circle = findViewById(R.id.step4_circle);

        // 체크 아이콘들 (step3Check, step4Check 추가)
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
    }

    // API 호출 부분
    private void fetchStatusFromAPI() {
        // 실제 API 호출 부분
        int reportId = getIntent().getIntExtra("report_id", 10);

        //RetrofitClient
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);

        Call<ReportStatusResponse> call = apiService.getReportStatus(reportId);

        call.enqueue(new Callback<ReportStatusResponse>() {
            @Override
            public void onResponse(Call<ReportStatusResponse> call, Response<ReportStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // API 응답 성공
                    ReportStatusResponse apiResponse = response.body();

                    // 받아온 데이터로 UI 업데이트
                    updateStatusUI(
                            String.valueOf(apiResponse.getReportId()),
                            apiResponse.getStatus()
                    );
                } else {
                    // API 응답 실패
                    handleApiError("서버 응답 오류: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ReportStatusResponse> call, Throwable t) {
                handleApiError("네트워크 오류: " + t.getMessage());
                Log.e("API_ERROR", "API 호출 실패", t);
            }
        });
    }

    private void updateStatusUI(String reportId, String statusMessage) {
        String fullText = reportId + " 신고건은\n현재 " + statusMessage + "이에요";
        mainAfterText.setText(fullText);

        // 상태 메시지 분석해서 현재 단계 판단
        int currentStep = determineStepFromStatus(statusMessage);
        updateStepProgress(currentStep);
    }

    // 상태 메시지를 분석해서 현재 단계를 판단하는 메서드
    private int determineStepFromStatus(String statusMessage) {
        // API에서 받을 수 있는 상태 메시지에 따라 현재 단계를 판단
        if (statusMessage.contains("접수") || statusMessage.contains("챗봇")) {
            return 1;
        } else if (statusMessage.contains("분석")) {
            return 2;
        } else if (statusMessage.contains("검토") || statusMessage.contains("HR팀")) {
            return 3;
        } else if (statusMessage.contains("답변") || (statusMessage.contains("대기"))) {
            return 4;
        }

        // 기본값
        return 1;
    }

    // 단계에 따라 UI 업데이트
    private void updateStepProgress(int currentStep) {
        // 모든 단계를 기본 상태(대기 상태)로 초기화
        resetAllSteps();

        // 현재 단계에 따라 각각 다른 UI 상태로 변경
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

            case 5:
                setStepCompleted(1);
                setStepCompleted(2);
                setStepCompleted(3);
                setStepCompleted(4);
                setDividerActive(divider1, true);
                setDividerActive(divider2, true);
                setDividerActive(divider3, true);
                break;
        }
    }

    // 상태 초기화
    private void resetAllSteps() {
        step1Circle.setBackgroundResource(R.drawable.ic_circle_stroke_mint);
        step2Circle.setBackgroundResource(R.drawable.ic_circle_stroke_mint);
        step3Circle.setBackgroundResource(R.drawable.ic_circle_stroke_mint);
        step4Circle.setBackgroundResource(R.drawable.ic_circle_stroke_mint);

        step1Check.setVisibility(View.GONE);
        step2Check.setVisibility(View.GONE);
        step3Check.setVisibility(View.GONE);
        step4Check.setVisibility(View.GONE);

        setTextSecondary(step1Text);
        setTextSecondary(step2Text);
        setTextSecondary(step3Text);
        setTextSecondary(step4Text);

        divider1.setBackgroundColor(ContextCompat.getColor(this, R.color.main_mint));
        divider2.setBackgroundColor(ContextCompat.getColor(this, R.color.main_mint));
        divider3.setBackgroundColor(ContextCompat.getColor(this, R.color.main_mint));
    }

    // 완료 상태 설정
    private void setStepCompleted(int stepNumber) {
        switch (stepNumber) {
            case 1:
                step1Circle.setBackgroundResource(R.drawable.ic_circle_stroke_skyblue);
                step1Check.setVisibility(View.VISIBLE);
                setTextSecondary(step1Text);
                break;
            case 2:
                step2Circle.setBackgroundResource(R.drawable.ic_circle_stroke_skyblue);
                step2Check.setVisibility(View.VISIBLE);
                setTextSecondary(step2Text);
                break;
            case 3:
                step3Circle.setBackgroundResource(R.drawable.ic_circle_stroke_skyblue);
                step3Check.setVisibility(View.VISIBLE);
                setTextSecondary(step3Text);
                break;
            case 4:
                step4Circle.setBackgroundResource(R.drawable.ic_circle_stroke_skyblue);
                step4Check.setVisibility(View.VISIBLE);
                setTextSecondary(step4Text);
                break;
        }
    }

    // 진행 중 상태 설정
    private void setStepInProgress(int stepNumber) {
        switch (stepNumber) {
            case 1:
                step1Circle.setBackgroundResource(R.drawable.ic_circle_filled_mint);
                step1Check.setVisibility(View.GONE);
                setTextPrimary(step1Text);
                break;
            case 2:
                step2Circle.setBackgroundResource(R.drawable.ic_circle_filled_mint);
                step2Check.setVisibility(View.GONE);
                setTextPrimary(step2Text);
                break;
            case 3:
                step3Circle.setBackgroundResource(R.drawable.ic_circle_filled_mint);
                step3Check.setVisibility(View.GONE);
                setTextPrimary(step3Text);
                break;
            case 4:
                step4Circle.setBackgroundResource(R.drawable.ic_circle_filled_mint);
                step4Check.setVisibility(View.GONE);
                setTextPrimary(step4Text);
                break;
        }
    }

    // 연결선 색상 설정
    private void setDividerActive(View divider, boolean isSkyblue) {
        if (isSkyblue) {
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.main_skyblue));
        } else {
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.main_mint));
        }
    }

    // 진행 중인 단계 텍스트 설정
    private void setTextPrimary(TextView textView) {
        textView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.roboto_semibold));
    }

    // 완료/대기 단계 텍스트 설정
    private void setTextSecondary(TextView textView) {
        textView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.roboto_medium));
    }

    // API 응답을 위한 데이터 모델 클래스
    public static class ReportStatusResponse {
        @SerializedName("report_id")
        public int reportId;
        @SerializedName("status")
        public String status;

        public ReportStatusResponse() {
        }

        public int getReportId() {
            return reportId;
        }

        public String getStatus() {
            return status;
        }
    }

    // Retrofit API 인터페이스
    public interface ApiService {
        // GET /api/report/{report_id} 엔드포인트 정의
        @GET("api/report/{report_id}")
        Call<ReportStatusResponse> getReportStatus(@Path("report_id") int reportId);
    }

    private void handleApiError(String errorMessage) {
        Log.e("API_ERROR", errorMessage);
        Toast.makeText(this, "데이터를 불러올 수 없습니다: " + errorMessage, Toast.LENGTH_LONG).show();
        updateStatusUI("--", "...");
    }

    // 메뉴 설정 - MenuBar 사용
    private void setupMenu() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ImageView menuButton = findViewById(R.id.menu);
        TextView menuClose = findViewById(R.id.menu_close);

        menuBar = new MenuBar(drawerLayout, menuButton, menuClose);
        menuBar.setupMenu();

        // 메뉴 열릴 때 세션 목록 갱신
        menuBar.setMenuBarListener(() -> sessionManager.fetchSessions());
    }

    @Override
    public void runOnMain(Runnable r) {
        runOnUiThread(r);
    }
}