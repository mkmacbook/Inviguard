package com.example.inviguard;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    private ImageView buttonSend;

    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private static final int REQUEST_CODE_PICK_AUDIO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.black_primary));
        }

        try {
            recyclerView = findViewById(R.id.recycler_view);
            editText = findViewById(R.id.edit_text);
            buttonSend = findViewById(R.id.button_send);
            ImageView backButton = findViewById(R.id.back);

            messageList = new ArrayList<>();
            chatAdapter = new ChatAdapter(messageList);

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(chatAdapter);

            backButton.setOnClickListener(v -> finish());

            // 리스너 등록
            chatAdapter.setOnButtonClickListener(this::handleUserInput);
            chatAdapter.setOnFilePickRequestedListener(type -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(type.equals("image") ? "image/*" : "audio/*");
                int code = type.equals("image") ? REQUEST_CODE_PICK_IMAGE : REQUEST_CODE_PICK_AUDIO;
                startActivityForResult(intent, code);
            });

            buttonSend.setOnClickListener(view -> {
                String userText = editText.getText().toString().trim();
                if (!userText.isEmpty()) {
                    editText.setText("");
                    handleUserInput(userText);
                }
            });

            createChatSession();
        } catch (Exception e) {
            Log.e("CrashCheck", "❌ onCreate 내부 예외: " + e.getMessage(), e);
        }
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

        String mappedInput = mapInput(input);
        Log.d("ChatState", "입력: " + input + " → 매핑: " + mappedInput + ", 현재 상태: " + currentState);

        getNextStateFromServer(currentState, mappedInput);
        saveUserMessage(sessionId, input);
    }

    private String mapInput(String input) {
        switch (input) {
            case "시작하기": return "start";
            case "네": return "yes";
            case "아니요": return "no";
            case "사진·음성 증거 더 추가하기": return "additional_evidence_upload";
            case "상황 설명 추가로 입력하기": return "additional_description";
            case "분석 시작하기": return "start_evaluation";
        }

        if ("prompt_general_description".equals(currentState)) {
            return "description_provided";
        }

        return input;
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
        try {
            String encodedState = URLEncoder.encode(state, "UTF-8");
            String url = "http://10.0.2.2:3000/api/chat/bot-message/state/" + encodedState;

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).get().build();

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
                            String message = json.getString("content");
                            String state = json.optString("state");

                            runOnUiThread(() -> {
                                addMessage(message, ChatMessage.TYPE_BOT);

                                // ✅ 파일 첨부 버튼 추가 조건
                                if ("wait_file_upload".equals(state)) {
                                    messageList.add(new ChatMessage(ChatMessage.TYPE_FILE_BUTTONS));
                                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                                    recyclerView.scrollToPosition(messageList.size() - 1);
                                }
                            });
                        } catch (JSONException e) {
                            runOnUiThread(() -> addMessage("❗JSON 파싱 오류", ChatMessage.TYPE_BOT));
                        }
                    } else {
                        runOnUiThread(() -> addMessage("❗응답 오류: " + response.code(), ChatMessage.TYPE_BOT));
                    }
                }
            });
        } catch (UnsupportedEncodingException e) {
            runOnUiThread(() -> addMessage("❗URL 인코딩 실패", ChatMessage.TYPE_BOT));
        }
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
        try {
            String formattedText = text.replaceAll("\\.\\s+", ".\n");
            if (formattedText.endsWith("\n")) {
                formattedText = formattedText.substring(0, formattedText.length() - 1);
            }

            messageList.add(new ChatMessage(formattedText, type));
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
        } catch (Exception e) {
            Log.e("MessageError", "❌ 메시지 추가 실패: " + e.getMessage(), e);
        }
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
