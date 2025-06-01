package com.example.inviguard;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// 메뉴바 목록 불러오기, 삭제를 위한 세션 매니저 구현
// 메인 전, 후에서 모두 필요해서 분리해놓은 용도
public class ChatSessionManager {
    private Context context;
    private LinearLayout sessionListLayout;

    public ChatSessionManager(Context context, LinearLayout sessionListLayout) {
        this.context = context;
        this.sessionListLayout = sessionListLayout;
    }

    // 세션 목록 조회
    public void fetchSessions() {
        String url = "http://10.0.2.2:3000/api/chat/sessions";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { e.printStackTrace(); }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    Log.d("ChatSession", "응답 데이터: " + json);
                    ((MainActivityInterface) context).runOnMain(() -> displaySessions(json));
                } else {
                    Log.e("ChatSession", "응답 실패: " + response.code());
                }
            }
        });
    }

    // 세션 목록 표시
    private void displaySessions(String json) {
        try {
            JSONArray array = new JSONArray(json);
            sessionListLayout.removeAllViews();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                int sessionId = obj.getInt("session_id");
                String summary = obj.getString("session_title");

                View item = LayoutInflater.from(context).inflate(R.layout.chat_item_session, sessionListLayout, false);
                TextView title = item.findViewById(R.id.session_title);
                ImageView deleteBtn = item.findViewById(R.id.session_delete);

                title.setText(summary);
                deleteBtn.setOnClickListener(v -> showDeleteDialog(sessionId, summary));
                sessionListLayout.addView(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 삭제 다이얼로그
    private void showDeleteDialog(int sessionId, String summary) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_proceed_report);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView title = dialog.findViewById(R.id.dialog_title);
        TextView message = dialog.findViewById(R.id.dialog_message);
        title.setText("채팅을 삭제하시겠습니까?");
        message.setText("선택하신 \"" + summary + "\" 채팅은 모두 삭제되며,\n삭제된 내용은 복구되지 않습니다.");

        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm);
        ImageView ivClose = dialog.findViewById(R.id.iv_close);

        btnCancel.setText("아니오");
        btnConfirm.setText("예");

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        ivClose.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            deleteSession(sessionId);
        });

        dialog.show();
    }

    // 삭제 요청
    private void deleteSession(int sessionId) {
        String url = "http://10.0.2.2:3000/api/chat/sessions/" + sessionId;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).delete().build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { e.printStackTrace(); }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    ((MainActivityInterface) context).runOnMain(() -> {
                        Toast.makeText(context, "삭제 완료", Toast.LENGTH_SHORT).show();
                        fetchSessions();
                    });
                }
            }
        });
    }

    public interface MainActivityInterface {
        void runOnMain(Runnable r);
    }
}
