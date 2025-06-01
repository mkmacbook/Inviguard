package com.example.inviguard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> messageList;

    // 🔹 버튼 클릭 리스너 인터페이스
    public interface OnButtonClickListener {
        void onButtonClicked(String buttonText);
    }

    private OnButtonClickListener buttonClickListener;

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.buttonClickListener = listener;
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
        } else if (viewType == ChatMessage.TYPE_SPACER) {
            // ✅ SpacerViewHolder 생성
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

            ((ButtonViewHolder) holder).button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (buttonClickListener != null) {
                        buttonClickListener.onButtonClicked(message.getMessage());
                    }
                }
            });
        }
        // ❗ SpacerViewHolder는 바인딩 처리 없음
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

    // 🔸 버튼 메시지 ViewHolder
    static class ButtonViewHolder extends RecyclerView.ViewHolder {
        TextView button;

        ButtonViewHolder(View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.button_message);
        }
    }

    // 🔸 Spacer 메시지 ViewHolder (빈 공간용)
    static class SpacerViewHolder extends RecyclerView.ViewHolder {
        SpacerViewHolder(View itemView) {
            super(itemView);
        }
    }
}
