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
        sessionId = getIntent().getIntExtra("session_id", -1);

        Log.d(TAG, "전달받은 session_id: " + sessionId);

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
            // details 배열에서 harassment_category_id와 severity 그대로 추출
            JSONArray detailsArray = sessionResult.optJSONArray("details");
            ArrayList<Integer> categoryIds = new ArrayList<>();
            ArrayList<Integer> severityList = new ArrayList<>();

            if (detailsArray != null) {
                for (int i = 0; i < detailsArray.length(); i++) {
                    JSONObject detail = detailsArray.getJSONObject(i);

                    int categoryId = detail.optInt("harassment_category_id", 0);
                    int severity = detail.optInt("severity", 0);

                    categoryIds.add(categoryId);
                    severityList.add(severity);

                    Log.d(TAG, "괴롭힘 카테고리 ID: " + categoryId + ", 심각도: " + severity);
                }
            }

            // ResultActivity로 이동 (2초 지연)
            runOnUiThread(() -> {
                // 2초 후에 ResultActivity로 이동
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    proceedToResult(sessionResult, categoryIds, severityList);
                }, 2000);
            });

        } catch (Exception e) {
            Log.e(TAG, "분석 결과 처리 실패", e);
            runOnUiThread(() -> {
                Toast.makeText(AnalyzingActivity.this, "분석 결과 처리에 실패했습니다.", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void proceedToResult(JSONObject sessionResult, ArrayList<Integer> categoryIds, ArrayList<Integer> severityList) {
        Intent intent = new Intent(AnalyzingActivity.this, ResultActivity.class);

        // 기본 세션 분석 결과
        intent.putExtra("session_id", sessionId);
        intent.putExtra("risk_score", sessionResult.optInt("risk_score", 0));
        intent.putExtra("is_harassment", sessionResult.optInt("is_harassment", 0));
        intent.putExtra("should_report", sessionResult.optInt("should_report", 0));
        intent.putExtra("session_eval_result_id", sessionResult.optInt("session_eval_result_id", 0));

        // 괴롭힘 상세 정보 (원본 데이터 그대로)
        intent.putIntegerArrayListExtra("category_ids", categoryIds);
        intent.putIntegerArrayListExtra("severity_list", severityList);

        Log.d(TAG, "ResultActivity로 이동 - 위험도: " + sessionResult.optInt("risk_score", 0) +
                ", 괴롭힘 카테고리 수: " + categoryIds.size());

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