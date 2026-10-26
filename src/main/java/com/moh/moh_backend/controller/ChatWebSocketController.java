package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.chat.ChatMessageRequest;
import com.moh.moh_backend.dto.chat.ChatMessageResponse;
import com.moh.moh_backend.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

    private final ChatService chatService;

    public ChatWebSocketController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Requirement 3 & 4:
     * Receives message over STOMP, persists in database first,
     * updates conversation unread counts & urgency, and broadcasts
     * to either /topic/phm-area/{phmAreaCode} (if Mother) or /topic/mother/{motherId} (if Midwife).
     *
     * STOMP Destination: /app/chat.sendMessage
     */
    @MessageMapping("/chat.sendMessage")
    public void handleIncomingMessage(@Payload ChatMessageRequest request) {
        log.info("Received STOMP message: senderId={}, role={}, isUrgent={}",
                request.getSenderId(), request.getSenderRole(), request.getIsUrgent());
        try {
            ChatMessageResponse response = chatService.sendMessage(request);
            log.info("Successfully persisted and dispatched message id={}", response.getMessageId());
        } catch (Exception e) {
            log.error("Failed to process WebSocket chat message", e);
        }
    }
}
