package com.moh.moh_backend.dto.chat;

import com.moh.moh_backend.model.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageResponse {

    private Long messageId;
    private Long conversationId;
    private Integer senderId;
    private String senderRole;
    private String content;
    private Boolean isUrgent;
    private Boolean isRead;
    private LocalDateTime timestamp;
    private String phmAreaCode;
    private Integer motherId;

    public static ChatMessageResponse fromEntity(ChatMessage message) {
        if (message == null) return null;
        return ChatMessageResponse.builder()
                .messageId(message.getMessageId())
                .conversationId(message.getConversation() != null ? message.getConversation().getConversationId() : null)
                .motherId(message.getConversation() != null ? message.getConversation().getMotherId() : null)
                .phmAreaCode(message.getConversation() != null ? message.getConversation().getPhmAreaCode() : null)
                .senderId(message.getSenderId())
                .senderRole(message.getSenderRole())
                .content(message.getContent())
                .isUrgent(message.getIsUrgent())
                .isRead(message.getIsRead())
                .timestamp(message.getTimestamp())
                .build();
    }
}
