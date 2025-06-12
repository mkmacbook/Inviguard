package com.example.inviguard;

public class ChatMessage {
    public static final int TYPE_USER = 0;     // 사용자가 입력한 메시지
    public static final int TYPE_BOT = 1;      // 챗봇의 일반 메시지
    public static final int TYPE_BUTTON = 2;

    public static final int TYPE_SPACER = 3;// 챗봇의 버튼 메시지 ← 추가
    public static final int TYPE_FILE_BUTTONS = 4; // 사진/오디오 첨부 버튼

    private String message;
    private int type;

    public ChatMessage(String message, int type) {
        this.message = message;
        this.type = type;
    }

    // 파일 첨부 버튼용 생성자 (message 없이 type만 전달)
    public ChatMessage(int type) {
        this.message = "";
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }
}
