package com.moh.moh_backend.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageRequest {

    private Long conversationId;

    private Integer motherId;

    @NotNull(message = "senderId is required")
    private Integer senderId;

    @NotBlank(message = "senderRole is required (MOTHER or MIDWIFE)")
    private String senderRole; // "MOTHER" or "MIDWIFE"

    @NotBlank(message = "content cannot be empty")
    private String content;

    @Builder.Default
    private Boolean isUrgent = false;

    private String phmAreaCode;
}
