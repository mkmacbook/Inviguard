package com.example.inviguard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import okhttp3.*;

public class AnalyzingActivity extends Activity {
    private static final String TAG = "AnalyzingActivity";
    private static final String BASE_URL = "http://10.0.2.2:3000/api/chat";
    private OkHttpClient client;
    private int sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyzing);

        Log.d(TAG, "=== AnalyzingActivity 시작 ===");

        client = new OkHttpClient();

        // Intent에서 session_id 받아오기
        sessionId = getIntent().getIntExtra("session_id", -1);

        Log.d(TAG, "전달받은 session_id: " + sessionId);
        Log.d(TAG, "BASE_URL: " + BASE_URL);

        if (sessionId == -1) {
            Log.e(TAG, "Invalid session_id - 앱 종료됨");
            Toast.makeText(this, "세션 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "세션 분석 시작...");
        startSessionAnalysis();
    }
    private void startSessionAnalysis() {
        // 실제 API 호출로 복구 (스키마가 올바르게 확인됨)
        String url = BASE_URL + "/analysis/sessions/" + sessionId;

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create("", MediaType.parse("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "세션 분석 요청 실패", e);
                runOnUiThread(() -> {
                    Toast.makeText(AnalyzingActivity.this, "분석 요청에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject result = new JSONObject(responseBody);

                        Log.d(TAG, "세션 분석 완료: " + responseBody);

                        // details 배열에서 harassment_category_id들 추출하고 프론트에서 매핑
                        processAnalysisResult(result);

                    } catch (Exception e) {
                        Log.e(TAG, "응답 파싱 실패", e);
                        runOnUiThread(() -> {
                            Toast.makeText(AnalyzingActivity.this, "분석 결과 처리에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                } else {
                    Log.e(TAG, "세션 분석 응답 오류: " + response.code());
                    runOnUiThread(() -> {
                        Toast.makeText(AnalyzingActivity.this, "분석에 실패했습니다. (오류 코드: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            }
        });
    }

    private void processAnalysisResult(JSONObject sessionResult) {
        try {
            // details 배열에서 harassment_category_id와 severity 추출
            JSONArray detailsArray = sessionResult.optJSONArray("details");
            ArrayList<String> harassmentTypes = new ArrayList<>();
            ArrayList<Double> severityList = new ArrayList<>(); // Double로 변경 (DB 스키마에 맞춤)

            if (detailsArray != null) {
                for (int i = 0; i < detailsArray.length(); i++) {
                    JSONObject detail = detailsArray.getJSONObject(i);
                    int categoryId = detail.getInt("harassment_category_id");
                    double severity = detail.getDouble("severity"); // Double로 변경

                    // 프론트에서 categoryId를 type name으로 매핑
                    String typeName = getHarassmentTypeName(categoryId);
                    harassmentTypes.add(typeName);
                    severityList.add(severity);

                    Log.d(TAG, "괴롭힘 유형: " + typeName + ", 심각도: " + severity);
                }
            }

            // ResultActivity로 이동
            runOnUiThread(() -> {
                proceedToResult(sessionResult, harassmentTypes, severityList);
            });

        } catch (Exception e) {
            Log.e(TAG, "분석 결과 처리 실패", e);
            runOnUiThread(() -> {
                Toast.makeText(AnalyzingActivity.this, "분석 결과 처리에 실패했습니다.", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    // harassment_category_id를 type name으로 매핑 (프론트에서 처리)
    private String getHarassmentTypeName(int categoryId) {
        // 백엔드 DB의 HarassmentCategory 테이블 구조에 맞춰 매핑
        switch (categoryId) {
            case 1: return "CENSURE";           // 경멸, 비난, 조롱 표현
            case 2: return "HATE";              // 특정 집단에 대한 혐오
            case 3: return "DISCRIMINATION";    // 차별 정당화 또는 편견 표현
            case 4: return "SEXUAL";            // 성적 대상화, 성희롱, 음란 표현
            case 5: return "VIOLENCE";          // 물리적·심리적 위협, 폭력성
            case 6: return "ABUSE";             // 욕설, 모욕, 감정적 폭력
            case 7: return "CRIME";             // 범죄 행위의 조장 또는 정당화
            case 8: return "VERBAL_ATTACK";     // 비하적 언어, 인신공격
            case 9: return "EXCESSIVE_WORKLOAD"; // 과도한 업무 부과
            case 10: return "UNFAIR_WORK_ORDERS"; // 부당한 업무 지시
            case 11: return "WORK_EXCLUSION";   // 부당한 업무 배제, 따돌림
            case 12: return "OBSTRUCTION_OF_WORK"; // 고의적 업무 방해
            case 13: return "INAPPROPRIATE_HR_ACTION"; // 불합리한 인사조치
            case 14: return "ECONOMIC_PRESSURE"; // 회식비 강요 등 경제적 부담
            case 15: return "SOCIAL_ISOLATION"; // 직장 내 고립, 따돌림
            default: return "UNKNOWN";
        }
    }

    // 괴롭힘 유형의 한국어 설명 반환
    private String getHarassmentTypeDescription(String type) {
        switch (type) {
            case "CENSURE": return "경멸, 비난, 조롱 표현";
            case "HATE": return "특정 집단에 대한 혐오";
            case "DISCRIMINATION": return "차별 정당화 또는 편견 표현";
            case "SEXUAL": return "성적 대상화, 성희롱, 음란 표현";
            case "VIOLENCE": return "물리적·심리적 위협, 폭력성";
            case "ABUSE": return "욕설, 모욕, 감정적 폭력";
            case "CRIME": return "범죄 행위의 조장 또는 정당화";
            case "VERBAL_ATTACK": return "비하적 언어, 인신공격";
            case "EXCESSIVE_WORKLOAD": return "과도한 업무 부과";
            case "UNFAIR_WORK_ORDERS": return "부당한 업무 지시, 원래 업무 외 지시";
            case "WORK_EXCLUSION": return "부당한 업무 배제, 따돌림";
            case "OBSTRUCTION_OF_WORK": return "고의적 업무 방해";
            case "INAPPROPRIATE_HR_ACTION": return "불합리한 인사조치";
            case "ECONOMIC_PRESSURE": return "회식비 강요 등 경제적 부담";
            case "SOCIAL_ISOLATION": return "직장 내 고립, 따돌림";
            default: return "알 수 없는 유형";
        }
    }

    // 심각도 레벨의 한국어 설명 반환 (DOUBLE 타입에 맞춤)
    private String getSeverityDescription(double severity) {
        if (severity < 0) return "특이사항 없음";
        else if (severity < 0.5) return "경미함";
        else if (severity < 1.0) return "비우호적 표현";
        else return "명백한 괴롭힘";
    }

    private void proceedToResult(JSONObject sessionResult, ArrayList<String> harassmentTypes, ArrayList<Double> severityList) {
        Intent intent = new Intent(AnalyzingActivity.this, ResultActivity.class);

        // 기본 세션 분석 결과
        intent.putExtra("session_id", sessionId);
        intent.putExtra("risk_score", sessionResult.optInt("risk_score", 0));
        intent.putExtra("is_harassment", sessionResult.optInt("is_harassment", 0));
        intent.putExtra("should_report", sessionResult.optInt("should_report", 0));
        intent.putExtra("session_eval_result_id", sessionResult.optInt("session_eval_result_id", 0));

        // 상세 괴롭힘 정보
        intent.putStringArrayListExtra("harassment_types", harassmentTypes);

        // Double을 String으로 변환해서 전달 (Android Intent는 ArrayList<Double> 지원 안함)
        ArrayList<String> severityStringList = new ArrayList<>();
        for (Double severity : severityList) {
            severityStringList.add(String.valueOf(severity));
        }
        intent.putStringArrayListExtra("severity_list", severityStringList);

        // 최대 심각도 계산
        double maxSeverity = 0.0;
        if (!severityList.isEmpty()) {
            for (double severity : severityList) {
                if (severity > maxSeverity) {
                    maxSeverity = severity;
                }
            }
        }
        intent.putExtra("max_severity", maxSeverity);

        // 괴롭힘 유형별 한국어 설명 배열도 함께 전달
        ArrayList<String> typeDescriptions = new ArrayList<>();
        for (String type : harassmentTypes) {
            typeDescriptions.add(getHarassmentTypeDescription(type));
        }
        intent.putStringArrayListExtra("type_descriptions", typeDescriptions);

        Log.d(TAG, "ResultActivity로 이동 - 위험도: " + sessionResult.optInt("risk_score", 0) +
                ", 괴롭힘 유형 수: " + harassmentTypes.size() + ", 최대 심각도: " + maxSeverity);

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null) {
            client.dispatcher().executorService().shutdown();
        }
    }
}