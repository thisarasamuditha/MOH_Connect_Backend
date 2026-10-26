package com.moh.moh_backend.dto.chat;

import com.moh.moh_backend.model.Conversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationResponse {

    private Long conversationId;
    private Integer motherId;
    private String motherName;
    private String phmAreaCode;
    private Integer phmAreaId;
    private String lastMessage;
    private Boolean lastMessageUrgent;
    private Integer unreadCountForMidwife;
    private Integer unreadCountForMother;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    public static ConversationResponse fromEntity(Conversation conv) {
        if (conv == null) return null;
        return ConversationResponse.builder()
                .conversationId(conv.getConversationId())
                .motherId(conv.getMotherId())
                .motherName(conv.getMotherName())
                .phmAreaCode(conv.getPhmAreaCode())
                .phmAreaId(conv.getPhmAreaId())
                .lastMessage(conv.getLastMessage())
                .lastMessageUrgent(conv.getLastMessageUrgent())
                .unreadCountForMidwife(conv.getUnreadCountForMidwife())
                .unreadCountForMother(conv.getUnreadCountForMother())
                .updatedAt(conv.getUpdatedAt())
                .createdAt(conv.getCreatedAt())
                .build();
    }
}
