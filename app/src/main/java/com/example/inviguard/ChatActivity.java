package com.example.inviguard;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
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

        // 상태바 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.black_primary));
        }

        recyclerView = findViewById(R.id.recycler_view);
        editText = findViewById(R.id.edit_text);
        buttonSend = findViewById(R.id.button_send);
        ImageView backButton = findViewById(R.id.back);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        backButton.setOnClickListener(v -> finish());

        chatAdapter.setOnButtonClickListener(this::sendUserInput);
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
                sendUserInput(userText);
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
                String responseBody = response.body().string();

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> addMessage("❗서버 오류: " + response.code(), ChatMessage.TYPE_BOT));
                    return;
                }

                try {
                    JSONObject json = new JSONObject(responseBody);
                    sessionId = json.getInt("session_id");
                    JSONObject botMessage = json.getJSONObject("bot_message");
                    runOnUiThread(() -> handleBotResponse(botMessage));
                } catch (JSONException e) {
                    runOnUiThread(() -> addMessage("❗초기 응답 오류", ChatMessage.TYPE_BOT));
                }
            }
        });
    }

    private String normalizeInputKey(String input) {
        input = input.trim().toLowerCase();
        switch (input) {
            case "시작하기":
                return "start";
            case "예":
            case "네":
            case "yes":
                return "yes";
            case "아니요":
            case "no":
                return "no";
            case "사진·음성 증거 더 추가하기":
                return "additional_evidence_upload";
            case "상황 설명 추가로 입력하기":
                return "additional_description";
            case "분석 시작하기":
                return "start_evaluation";
            default:
                return input;
        }
    }

    private void sendUserInput(String input) {
        addMessage(input, ChatMessage.TYPE_USER);
        if (sessionId == -1) {
            addMessage("❗세션이 생성되지 않았습니다", ChatMessage.TYPE_BOT);
            return;
        }

        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:3000/api/chat/sessions/" + sessionId + "/messages";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("content", input);
            jsonBody.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        } catch (JSONException e) {
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> addMessage("❗메시지 전송 실패", ChatMessage.TYPE_BOT));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String normalizedInput = normalizeInputKey(input);
                    runStateTransitionFlow(normalizedInput);
                } else {
                    runOnUiThread(() -> addMessage("❗메시지 저장 실패", ChatMessage.TYPE_BOT));
                }
            }
        });
    }

    private void runStateTransitionFlow(String input) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:3000/api/chat/state/next?currentState=" + currentState + "&input=" + input;

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> addMessage("❗상태 전이 실패", ChatMessage.TYPE_BOT));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> addMessage("❗서버 오류: " + response.code(), ChatMessage.TYPE_BOT));
                    return;
                }

                try {
                    JSONObject json = new JSONObject(responseBody);
                    String nextState = json.optString("nextState", null);

                    if (nextState == null || nextState.equals("null")) {
                        runOnUiThread(() -> addMessage("❗상태 전이 규칙을 찾을 수 없습니다", ChatMessage.TYPE_BOT));
                        return;
                    }

                    updateSessionState(nextState);
                } catch (JSONException e) {
                    runOnUiThread(() -> addMessage("❗전이 응답 오류", ChatMessage.TYPE_BOT));
                }
            }
        });
    }

    private void updateSessionState(String newState) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:3000/api/chat/sessions/" + sessionId + "/state";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("newState", newState);
        } catch (JSONException e) {
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder().url(url).patch(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> addMessage("❗세션 상태 저장 실패", ChatMessage.TYPE_BOT));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    fetchNextMessage(newState);
                } else {
                    runOnUiThread(() -> addMessage("❗상태 저장 실패", ChatMessage.TYPE_BOT));
                }
            }
        });
    }

    private void fetchNextMessage(String state) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:3000/api/chat/state/" + state;

        Request request = new Request.Builder().url(url).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> addMessage("❗다음 메시지 로딩 실패", ChatMessage.TYPE_BOT));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> addMessage("❗메시지 로딩 실패: " + response.code(), ChatMessage.TYPE_BOT));
                    return;
                }

                try {
                    JSONObject botMsg = new JSONObject(responseBody);
                    currentState = state;
                    runOnUiThread(() -> handleBotResponse(botMsg));
                } catch (JSONException e) {
                    runOnUiThread(() -> addMessage("❗다음 메시지 파싱 실패", ChatMessage.TYPE_BOT));
                }
            }
        });
    }

    private void handleBotResponse(JSONObject botMsg) {
        String content = botMsg.optString("message", botMsg.optString("content", ""));
        String state = botMsg.optString("state", currentState);

        if (!content.isEmpty()) {
            addMessage(content, ChatMessage.TYPE_BOT);
        }

        if (state == null || state.isEmpty()) return;
        currentState = state;

        switch (state) {
            case "intro":
                messageList.add(new ChatMessage("시작하기", ChatMessage.TYPE_BUTTON));
                break;
            case "wait_file_upload":
                messageList.add(new ChatMessage(ChatMessage.TYPE_FILE_BUTTONS));
                break;
            case "review_before_evaluation":
                messageList.add(new ChatMessage(ChatMessage.TYPE_REVIEW_OPTIONS));
                break;
            default:
                break;
        }

        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
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
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            String type = requestCode == REQUEST_CODE_PICK_IMAGE ? "image" : "audio";
            uploadFileToServer(uri, type);
        }
    }

    private void uploadFileToServer(Uri uri, String type) {
        try {
            String fileName = "upload." + (type.equals("image") ? "jpg" : "mp3");

            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                addMessage("❗파일 열기 실패", ChatMessage.TYPE_BOT);
                return;
            }

            RequestBody fileBody = new RequestBody() {
                @Override
                public MediaType contentType() {
                    return MediaType.parse(type + "/*");
                }

                @Override
                public void writeTo(okio.BufferedSink sink) throws IOException {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        sink.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                }
            };

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", fileName, fileBody)
                    .build();

            String url = "http://10.0.2.2:3000/api/chat/sessions/" + sessionId + "/evidence";

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> addMessage("❗파일 업로드 실패", ChatMessage.TYPE_BOT));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();

                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            addMessage(" 파일 업로드 성공", ChatMessage.TYPE_BOT);
                            runStateTransitionFlowWithSystemEvent("file_uploaded");
                        });
                    } else {
                        runOnUiThread(() -> addMessage("❗파일 업로드 실패: " + response.code(), ChatMessage.TYPE_BOT));
                    }
                }
            });

        } catch (Exception e) {
            addMessage("❗업로드 중 오류 발생", ChatMessage.TYPE_BOT);
        }
    }

    private void runStateTransitionFlowWithSystemEvent(String systemEvent) {
        if (sessionId == -1) return;

        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:3000/api/chat/state/next?currentState=" + currentState + "&systemEvent=" + systemEvent;

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> addMessage("❗상태 전이 실패", ChatMessage.TYPE_BOT));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> addMessage("❗서버 오류: " + response.code(), ChatMessage.TYPE_BOT));
                    return;
                }

                try {
                    JSONObject json = new JSONObject(responseBody);
                    String nextState = json.optString("nextState", null);

                    if (nextState == null || nextState.equals("null")) {
                        runOnUiThread(() -> addMessage("❗상태 전이 규칙을 찾을 수 없습니다", ChatMessage.TYPE_BOT));
                        return;
                    }

                    updateSessionState(nextState);
                } catch (JSONException e) {
                    runOnUiThread(() -> addMessage("❗전이 응답 오류", ChatMessage.TYPE_BOT));
                }
            }
        });
    }
}