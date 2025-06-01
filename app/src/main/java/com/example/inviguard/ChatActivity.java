package com.example.inviguard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private int sessionId = -1;

    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private static final int REQUEST_CODE_PICK_AUDIO = 2;

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private EditText editText;
    private ImageButton buttonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recycler_view);
        editText = findViewById(R.id.edit_text);
        buttonSend = findViewById(R.id.button_send);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        chatAdapter.setOnButtonClickListener(this::handleUserInput);
        buttonSend.setOnClickListener(view -> {
            String userText = editText.getText().toString().trim();
            if (!userText.isEmpty()) {
                editText.setText("");
                handleUserInput(userText);
            }
        });

        // ✅ 세션 생성 후 intro 메시지를 자동으로 받아오기
        createChatSession();
    }

    private void createChatSession() {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:3000/api/chat/sessions";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", 1); // 실제 사용자 ID
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> addMessage("❗서버 연결 실패", ChatMessage.TYPE_BOT));
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        sessionId = json.getInt("session_id");
                        String content = json.getJSONObject("bot_message").getString("content");

                        runOnUiThread(() -> {
                            addMessage(content, ChatMessage.TYPE_BOT);
                            addMessage("시작하기", ChatMessage.TYPE_BUTTON);  // 처음에 버튼 추가
                        });

                    } catch (JSONException e) {
                        runOnUiThread(() -> addMessage("❗JSON 파싱 오류", ChatMessage.TYPE_BOT));
                    }
                } else {
                    runOnUiThread(() -> addMessage("❗응답 오류: " + response.code(), ChatMessage.TYPE_BOT));
                }
            }
        });
    }

    // ✅ 핵심 함수: 사용자 입력을 서버에 POST
    // 사용자가 입력하면 호출되는 함수 내부
    private void handleUserInput(String input) {
        addMessage(input, ChatMessage.TYPE_USER);

        if (sessionId == -1) {
            addMessage("❗세션 ID가 없습니다.", ChatMessage.TYPE_BOT);
            return;
        }

        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:3000/api/chat/sessions/" + sessionId + "/messages";

        JSONObject jsonBody = new JSONObject();
        try {
            // ✅ 시간 형식 수정
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String formattedTime = sdf.format(new Date());

            jsonBody.put("content", input);
            jsonBody.put("timestamp", formattedTime);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                jsonBody.toString(),
                okhttp3.MediaType.parse("application/json")
        );

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> addMessage("❗서버 연결 실패", ChatMessage.TYPE_BOT));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        String botMessage = json.getJSONObject("bot_message").getString("content");

                        runOnUiThread(() -> addMessage(botMessage, ChatMessage.TYPE_BOT));
                    } catch (JSONException e) {
                        runOnUiThread(() -> addMessage("❗응답 파싱 오류", ChatMessage.TYPE_BOT));
                    }
                } else {
                    runOnUiThread(() -> addMessage("❗응답 오류: " + response.code(), ChatMessage.TYPE_BOT));
                }
            }
        });
    }



    private void addMessage(String text, int type) {
        messageList.add(new ChatMessage(text, type));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void pickFile(String type, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(type);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            String uri = data.getData().toString();
            if (requestCode == REQUEST_CODE_PICK_IMAGE) {
                addMessage("📸 사진 첨부됨: " + uri, ChatMessage.TYPE_BOT);
            } else if (requestCode == REQUEST_CODE_PICK_AUDIO) {
                addMessage("🎤 오디오 첨부됨: " + uri, ChatMessage.TYPE_BOT);
            }
        }
    }
}