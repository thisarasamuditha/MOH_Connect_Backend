package com.moh.moh_backend.controller;

import com.moh.moh_backend.dto.chat.ChatMessageRequest;
import com.moh.moh_backend.dto.chat.ChatMessageResponse;
import com.moh.moh_backend.dto.chat.ConversationResponse;
import com.moh.moh_backend.service.ChatService;
import com.moh.moh_backend.util.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/chat", "/chat"})
public class ChatController {

    private final ChatService chatService;
    private final JwtService jwtService;

    public ChatController(ChatService chatService,JwtService jwtService) {
        this.chatService = chatService;
        this.jwtService  = jwtService;
    }

    /**
     * Requirement 3: Midwife logs in and fetches all conversations for her area
     * ordered by updatedAt DESC, displaying unread counts and urgent flags.
     * GET /api/chat/midwife/inbox/{phmAreaId}
     */
    @GetMapping("/midwife/inbox/{phmAreaId}")
    public ResponseEntity<List<ConversationResponse>> getMidwifeInbox(@PathVariable String phmAreaId) {
        List<ConversationResponse> inbox = chatService.getMidwifeInbox(phmAreaId);
        return ResponseEntity.ok(inbox);
    }

    /**
     * Get or initialize a Mother's conversation with her assigned Midwife
     * GET /api/chat/mother/conversation/{motherId}
     */
    @GetMapping("/mother/conversation/{motherId}")
    public ResponseEntity<ConversationResponse> getMotherConversation(@PathVariable Integer motherId) {
        ConversationResponse conversation = chatService.getOrCreateMotherConversation(motherId);
        return ResponseEntity.ok(conversation);
    }

    /**
     * Fetch conversation message history ordered chronologically
     * GET /api/chat/conversations/{conversationId}/messages
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getConversationMessages(@PathVariable Long conversationId) {
        List<ChatMessageResponse> messages = chatService.getConversationMessages(conversationId);
        return ResponseEntity.ok(messages);
    }

    /**
     * REST endpoint to send message (HTTP fallback for WebSocket)
     * POST /api/chat/messages
     */
    @PostMapping("/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(@Valid @RequestBody ChatMessageRequest request) {
        ChatMessageResponse response = chatService.sendMessage(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Requirement 5: Read Receipts
     * PUT /api/chat/conversations/{conversationId}/read?readerRole=MIDWIFE|MOTHER
     */
    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<ConversationResponse> markAsRead(
            @PathVariable Long conversationId,
            jakarta.servlet.http.HttpServletRequest
                    request) {
        Integer userId = (Integer) request.getAttribute("moh.userId");
        String role = (String) request.getAttribute("moh.role");
        ConversationResponse updated = chatService.markAsRead(conversationId, role);
        return ResponseEntity.ok(updated);
    }
}
