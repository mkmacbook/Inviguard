package com.example.inviguard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private String currentState = "intro";

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private EditText editText;
    private ImageButton buttonSend;

    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private static final int REQUEST_CODE_PICK_AUDIO = 2;

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

        createChatSession();
    }

    private void createChatSession() {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:3000/api/chat/sessions";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", 1);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> addMessage("❗서버 연결 실패", ChatMessage.TYPE_BOT));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        sessionId = json.getInt("session_id");

                        JSONObject botMessage = json.optJSONObject("bot_message");
                        if (botMessage != null && botMessage.has("content")) {
                            String content = botMessage.getString("content");
                            currentState = botMessage.optString("state", "intro");

                            runOnUiThread(() -> {
                                addMessage(content, ChatMessage.TYPE_BOT);
                                addMessage("시작하기", ChatMessage.TYPE_BUTTON);
                            });
                        } else {
                            runOnUiThread(() -> addMessage("❗초기 메시지를 불러올 수 없어요", ChatMessage.TYPE_BOT));
                        }
                    } catch (JSONException e) {
                        runOnUiThread(() -> addMessage("❗JSON 파싱 오류", ChatMessage.TYPE_BOT));
                    }
                } else {
                    runOnUiThread(() -> addMessage("❗응답 오류: " + response.code(), ChatMessage.TYPE_BOT));
                }
            }
        });
    }

    private void handleUserInput(String input) {
        addMessage(input, ChatMessage.TYPE_USER);
        if (sessionId == -1) {
            addMessage("❗세션 ID가 없습니다.", ChatMessage.TYPE_BOT);
            return;
        }

        Log.d("ChatState", "입력: " + input + ", 현재 상태: " + currentState);

        getNextStateFromServer(currentState, input);
        saveUserMessage(sessionId, input);
    }

    private void getNextStateFromServer(String current, String input) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:3000/api/chat/state/next?currentState=" + current + "&input=" + input;

        Log.d("ChatState", "GET → " + url);

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> addMessage("❗다음 상태 가져오기 실패", ChatMessage.TYPE_BOT));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        String nextState = json.optString("nextState", null);

                        Log.d("ChatState", "다음 상태: " + nextState);

                        if (nextState != null && !nextState.isEmpty()) {
                            currentState = nextState;
                            fetchBotMessageByState(nextState);
                        } else {
                            runOnUiThread(() -> addMessage("❗서버에서 다음 상태를 찾지 못했어요", ChatMessage.TYPE_BOT));
                        }
                    } catch (JSONException e) {
                        runOnUiThread(() -> addMessage("❗JSON 파싱 오류", ChatMessage.TYPE_BOT));
                    }
                } else {
                    runOnUiThread(() -> addMessage("❗응답 오류: " + response.code(), ChatMessage.TYPE_BOT));
                }
            }
        });
    }

    private void fetchBotMessageByState(String state) {
        Log.d("FetchState", "✅ fetchBotMessageByState() 함수 호출됨, state: " + state);
        String url = "http://10.0.2.2:3000/api/chat/bot-message/state/" + state;
        Log.d("FetchState", "🔍 요청 URL: " + url);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("FetchState", "❌ 서버 연결 실패: " + e.getMessage());
                runOnUiThread(() -> addMessage("❗서버 연결 실패", ChatMessage.TYPE_BOT));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("FetchState", "📥 응답 코드: " + response.code());
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        Log.d("FetchState", "✅ 응답 본문: " + responseData);

                        JSONObject json = new JSONObject(responseData);
                        String message = json.getString("content");

                        runOnUiThread(() -> addMessage(message, ChatMessage.TYPE_BOT));
                    } catch (JSONException e) {
                        Log.d("FetchState", "❗JSON 파싱 오류: " + e.getMessage());
                        runOnUiThread(() -> addMessage("❗JSON 파싱 오류", ChatMessage.TYPE_BOT));
                    }
                } else {
                    Log.d("FetchState", "⚠️ 응답 오류: " + response.code());
                    runOnUiThread(() -> addMessage("❗응답 오류: " + response.code(), ChatMessage.TYPE_BOT));
                }
            }
        });
    }


    private void saveUserMessage(int sessionId, String input) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:3000/api/chat/sessions/" + sessionId + "/messages";

        JSONObject jsonBody = new JSONObject();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String formattedTime = sdf.format(new Date());
            jsonBody.put("content", input);
            jsonBody.put("timestamp", formattedTime);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) throws IOException {}
        });
    }

    private void addMessage(String text, int type) {
        messageList.add(new ChatMessage(text, type));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
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
