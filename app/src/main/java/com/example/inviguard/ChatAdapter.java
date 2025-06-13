package com.example.inviguard;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> messageList;

    // 🔹 단일 버튼 클릭 리스너
    public interface OnButtonClickListener {
        void onButtonClicked(String buttonText);
    }

    private OnButtonClickListener buttonClickListener;

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.buttonClickListener = listener;
    }

    // 🔹 파일 첨부 버튼 리스너
    public interface OnFilePickRequestedListener {
        void onFilePickRequested(String type); // "image" or "audio"
    }

    private OnFilePickRequestedListener filePickListener;

    public void setOnFilePickRequestedListener(OnFilePickRequestedListener listener) {
        this.filePickListener = listener;
    }

    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == ChatMessage.TYPE_USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_user, parent, false);
            return new UserViewHolder(view);
        } else if (viewType == ChatMessage.TYPE_BOT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_bot, parent, false);
            return new BotViewHolder(view);
        } else if (viewType == ChatMessage.TYPE_BUTTON) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_button, parent, false);
            return new ButtonViewHolder(view);
        } else if (viewType == ChatMessage.TYPE_FILE_BUTTONS) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_file_buttons, parent, false);
            return new FileButtonViewHolder(view);
        } else if (viewType == ChatMessage.TYPE_USER_IMAGE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_user_image, parent, false);
            return new UserImageViewHolder(view);
        } else if (viewType == ChatMessage.TYPE_USER_AUDIO) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_user_audio, parent, false);
            return new UserAudioViewHolder(view);
        } else if (viewType == ChatMessage.TYPE_SPACER) {
            View spacerView = new View(parent.getContext());
            int heightInPx = (int) (32 * parent.getContext().getResources().getDisplayMetrics().density); // 32dp
            spacerView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    heightInPx
            ));
            return new SpacerViewHolder(spacerView);
        } else {
            throw new IllegalArgumentException("Unknown view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).textView.setText(message.getMessage());
        } else if (holder instanceof BotViewHolder) {
            ((BotViewHolder) holder).textView.setText(message.getMessage());
        } else if (holder instanceof ButtonViewHolder) {
            ((ButtonViewHolder) holder).button.setText(message.getMessage());
            ((ButtonViewHolder) holder).button.setOnClickListener(v -> {
                if (buttonClickListener != null) {
                    buttonClickListener.onButtonClicked(message.getMessage());
                }
            });
        } else if (holder instanceof FileButtonViewHolder) {
            ((FileButtonViewHolder) holder).bind();
        } else if (holder instanceof UserImageViewHolder) { // 이미지 바인딩
            ((UserImageViewHolder) holder).bind(message);
        } else if (holder instanceof UserAudioViewHolder) { // 오디오 바인딩
            ((UserAudioViewHolder) holder).bind(message);
        }
        // SpacerViewHolder는 바인딩 없음
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // 🔸 사용자 메시지 ViewHolder
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        UserViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_message);
        }
    }

    // 🔸 챗봇 메시지 ViewHolder
    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        BotViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_message);
        }
    }

    // 🔸 단일 버튼 메시지 ViewHolder
    static class ButtonViewHolder extends RecyclerView.ViewHolder {
        TextView button;

        ButtonViewHolder(View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.button_message);
        }
    }

    // 🔸 파일 첨부 버튼 ViewHolder
    class FileButtonViewHolder extends RecyclerView.ViewHolder {
        Button buttonImage, buttonAudio;

        FileButtonViewHolder(View itemView) {
            super(itemView);
            buttonImage = itemView.findViewById(R.id.button_image);
            buttonAudio = itemView.findViewById(R.id.button_audio);
        }

        void bind() {
            buttonImage.setOnClickListener(v -> {
                if (filePickListener != null) filePickListener.onFilePickRequested("image");
            });
            buttonAudio.setOnClickListener(v -> {
                if (filePickListener != null) filePickListener.onFilePickRequested("audio");
            });
        }
    }

    // 사용자 이미지 메시지 ViewHolder
    static class UserImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        UserImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_message);
        }

        void bind(ChatMessage message) {
            if (message.getFileUri() != null) {
                // Glide를 사용해서 이미지 로드 (둥근 모서리 적용)
                Glide.with(itemView.getContext())
                        .load(Uri.parse(message.getFileUri()))
                        .centerCrop()
                        .transform(new RoundedCorners(24)) // 둥근 모서리 적용 (24dp)
                        .into(imageView);
            }
        }

    }

    // 사용자 오디오 메시지 ViewHolder
    static class UserAudioViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        UserAudioViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_message);
        }

        void bind(ChatMessage message) {
            textView.setText(message.getMessage());
        }
    }

    // 🔸 Spacer ViewHolder
    static class SpacerViewHolder extends RecyclerView.ViewHolder {
        SpacerViewHolder(View itemView) {
            super(itemView);
        }
    }
}