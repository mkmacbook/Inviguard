package com.example.inviguard;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private EditText editText;
    private ImageButton buttonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 🔧 레이아웃 연결
        recyclerView = findViewById(R.id.recycler_view);
        editText = findViewById(R.id.edit_text);
        buttonSend = findViewById(R.id.button_send);

        // 🔧 메시지 리스트 초기화
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // ✅ 초기 챗봇 메시지 추가
        addMessage("안녕하세요 저는 inviGuard의 AI 챗봇 이음이에요. 직장에서 힘든 일을 겪으셨다면, 편하게 말씀해주세요.", ChatMessage.TYPE_BOT);
        addMessage("👉 시작하기", ChatMessage.TYPE_BOT);  // 버튼처럼 보이는 텍스트

        // ✅ 버튼 클릭 이벤트
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userText = editText.getText().toString().trim();
                if (!userText.isEmpty()) {
                    addMessage(userText, ChatMessage.TYPE_USER);
                    editText.setText("");

                    // ✅ "시작하기" 입력 시 특별한 응답
                    if (userText.equals("시작하기") || userText.equalsIgnoreCase("시작하기")) {
                        addMessage("최근에 겪으신 불편한 일이 있다면 자유롭게 말씀해주세요.", ChatMessage.TYPE_BOT);
                    } else {
                        // 일반 응답
                        addMessage("잘 알겠습니다. 이어서 말씀해 주세요.", ChatMessage.TYPE_BOT);
                    }
                }
            }
        });
    }

    private void addMessage(String text, int type) {
        messageList.add(new ChatMessage(text, type));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }
}
