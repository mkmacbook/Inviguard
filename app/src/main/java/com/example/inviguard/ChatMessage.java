package com.example.inviguard;

public class ChatMessage {
    public static final int TYPE_USER = 0;     // 사용자가 입력한 메시지
    public static final int TYPE_BOT = 1;      // 챗봇의 일반 메시지
    public static final int TYPE_BUTTON = 2;
    public static final int TYPE_SPACER = 3;   // 챗봇의 버튼 메시지 ← 추가
    public static final int TYPE_FILE_BUTTONS = 4; // 사진/오디오 첨부 버튼
    public static final int TYPE_REVIEW_OPTIONS = 5; // 분석 전 옵션 선택 버튼
    public static final int TYPE_USER_IMAGE = 6; // 사용자 이미지 메시지
    public static final int TYPE_USER_AUDIO = 7; // 사용자 오디오 메시지

    private String message;
    private int type;
    private String fileUri; // 이미지/오디오 파일 URI 저장

    // 기존 생성자들
    public ChatMessage(String message, int type) {
        this.message = message;
        this.type = type;
    }

    // 파일 첨부 버튼용 생성자 (message 없이 type만 전달)
    public ChatMessage(int type) {
        this.message = "";
        this.type = type;
    }

    // 파일(이미지/오디오)를 위한 생성자
    public ChatMessage(String message, int type, String fileUri) {
        this.message = message;
        this.type = type;
        this.fileUri = fileUri;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    // 파일 URI getter
    public String getFileUri() {
        return fileUri;
    }

    // 이미지인지 확인하는 헬퍼 메서드
    public boolean isImage() {
        return type == TYPE_USER_IMAGE;
    }

    // 오디오인지 확인하는 헬퍼 메서드
    public boolean isAudio() {
        return type == TYPE_USER_AUDIO;
    }
}