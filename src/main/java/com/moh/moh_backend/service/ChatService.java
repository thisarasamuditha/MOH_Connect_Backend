package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.chat.ChatMessageRequest;
import com.moh.moh_backend.dto.chat.ChatMessageResponse;
import com.moh.moh_backend.dto.chat.ConversationResponse;
import com.moh.moh_backend.model.ChatMessage;
import com.moh.moh_backend.model.Conversation;
import com.moh.moh_backend.model.Midwife;
import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.repository.ChatMessageRepository;
import com.moh.moh_backend.repository.ConversationRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.MotherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MotherRepository motherRepository;
    private final MidwifeRepository midwifeRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(ConversationRepository conversationRepository,
                       ChatMessageRepository chatMessageRepository,
                       MotherRepository motherRepository,
                       MidwifeRepository midwifeRepository,
                       SimpMessagingTemplate messagingTemplate) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.motherRepository = motherRepository;
        this.midwifeRepository = midwifeRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Persist chat message and route to respective WebSocket topics.
     * Offline Support: Message is saved in DB first and updates unread counts
     * before STOMP broadcast.
     */
    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be blank");
        }
        String senderRole = request.getSenderRole() != null ? request.getSenderRole().trim().toUpperCase() : "";
        if (!"MOTHER".equals(senderRole) && !"MIDWIFE".equals(senderRole)) {
            throw new IllegalArgumentException("Invalid senderRole: must be MOTHER or MIDWIFE");
        }

        // 1. Resolve or Create Conversation
        Conversation conversation = resolveOrCreateConversation(request);

        // 2. Validate PHM Area Isolation
        validatePhmAreaAccess(conversation, request.getSenderId(), senderRole);

        // 3. Update Conversation Metadata and Unread Counts
        boolean isUrgent = Boolean.TRUE.equals(request.getIsUrgent());
        conversation.setLastMessage(request.getContent().trim());
        conversation.setLastMessageUrgent(isUrgent);
        conversation.setUpdatedAt(LocalDateTime.now());

        if ("MOTHER".equals(senderRole)) {
            int currentUnread = conversation.getUnreadCountForMidwife() != null ? conversation.getUnreadCountForMidwife() : 0;
            conversation.setUnreadCountForMidwife(currentUnread + 1);
        } else {
            int currentUnread = conversation.getUnreadCountForMother() != null ? conversation.getUnreadCountForMother() : 0;
            conversation.setUnreadCountForMother(currentUnread + 1);
        }
        conversation = conversationRepository.save(conversation);

        // 4. Save Chat Message Entity
        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .senderId(request.getSenderId())
                .senderRole(senderRole)
                .content(request.getContent().trim())
                .isUrgent(isUrgent)
                .isRead(false)
                .timestamp(LocalDateTime.now())
                .build();
        message = chatMessageRepository.save(message);

        ChatMessageResponse response = ChatMessageResponse.fromEntity(message);

        // 5. Real-Time STOMP Routing
        broadcastMessage(conversation, response, senderRole);

        return response;
    }

    /**
     * Broadcasts to specific channels according to core business rules:
     * - Mother sends -> /topic/phm-area/{phmAreaCode} (for online Midwives in that area)
     * - Midwife replies -> /topic/mother/{motherId} (for that specific Mother)
     * - Both parties also receive on /topic/conversation/{conversationId} for active chat tabs
     */
    private void broadcastMessage(Conversation conversation, ChatMessageResponse response, String senderRole) {
        try {
            // General conversation channel
            messagingTemplate.convertAndSend("/topic/conversation/" + conversation.getConversationId(), response);

            if ("MOTHER".equals(senderRole)) {
                // Route to all midwives in the mother's registered PHM Area
                String areaTopic = "/topic/phm-area/" + conversation.getPhmAreaCode();
                messagingTemplate.convertAndSend(areaTopic, response);
                log.info("Broadcasted Mother message to PHM Area topic: {}", areaTopic);

                // Also notify midwife inbox channel with updated conversation metadata
                ConversationResponse convDto = ConversationResponse.fromEntity(conversation);
                messagingTemplate.convertAndSend("/topic/phm-area/" + conversation.getPhmAreaCode() + "/inbox", convDto);
            } else {
                // Route to specific Mother
                String motherTopic = "/topic/mother/" + conversation.getMotherId();
                messagingTemplate.convertAndSend(motherTopic, response);
                log.info("Broadcasted Midwife message to Mother topic: {}", motherTopic);
            }
        } catch (Exception ex) {
            log.error("Failed to broadcast message via WebSocket STOMP", ex);
        }
    }

    /**
     * Fetch all conversations for a Midwife's PHM Area, ordered by updatedAt DESC
     */
    @Transactional(readOnly = true)
    public List<ConversationResponse> getMidwifeInbox(String phmAreaIdentifier) {
        List<Conversation> conversations;
        try {
            Integer areaId = Integer.parseInt(phmAreaIdentifier.trim());
            conversations = conversationRepository.findByAreaIdOrCode(areaId, phmAreaIdentifier.trim());
        } catch (NumberFormatException e) {
            conversations = conversationRepository.findByPhmAreaCodeOrderByUpdatedAtDesc(phmAreaIdentifier.trim());
        }

        return conversations.stream()
                .map(ConversationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get or initialize a Mother's conversation thread
     */
    @Transactional
    public ConversationResponse getOrCreateMotherConversation(Integer motherId) {
        Conversation conv = conversationRepository.findByMotherId(motherId)
                .orElseGet(() -> {
                    Mother mother = motherRepository.findById(motherId)
                            .or(() -> motherRepository.findByUser_UserId(motherId))
                            .orElseThrow(() -> new IllegalArgumentException("Mother not found with ID or User ID: " + motherId));

                    String areaCode = mother.getPhmArea() != null ? mother.getPhmArea().getAreaCode() : "DEFAULT";
                    Integer areaId = mother.getPhmArea() != null ? mother.getPhmArea().getPhmAreaId() : null;

                    Conversation newConv = Conversation.builder()
                            .motherId(mother.getMotherId())
                            .motherName(mother.getName())
                            .phmAreaCode(areaCode)
                            .phmAreaId(areaId)
                            .lastMessage(null)
                            .lastMessageUrgent(false)
                            .unreadCountForMidwife(0)
                            .unreadCountForMother(0)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return conversationRepository.save(newConv);
                });

        return ConversationResponse.fromEntity(conv);
    }

    /**
     * Get all chat messages for a conversation
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getConversationMessages(Long conversationId) {
        return chatMessageRepository.findByConversation_ConversationIdOrderByTimestampAsc(conversationId)
                .stream()
                .map(ChatMessageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Mark conversation as read and reset unread counts
     */
    @Transactional
    public ConversationResponse markAsRead(Long conversationId, String readerRole) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));

        String role = readerRole != null ? readerRole.trim().toUpperCase() : "";
        if ("MIDWIFE".equals(role)) {
            conversation.setUnreadCountForMidwife(0);
            chatMessageRepository.markMessagesAsReadForRole(conversationId, "MOTHER");
        } else if ("MOTHER".equals(role)) {
            conversation.setUnreadCountForMother(0);
            chatMessageRepository.markMessagesAsReadForRole(conversationId, "MIDWIFE");
        } else {
            throw new IllegalArgumentException("Invalid readerRole: must be MIDWIFE or MOTHER");
        }

        conversation = conversationRepository.save(conversation);
        ConversationResponse response = ConversationResponse.fromEntity(conversation);

        // Notify client of read receipt
        try {
            Object readPayload = Map.of(
                    "conversationId", conversationId,
                    "readerRole", role,
                    "timestamp", LocalDateTime.now().toString()
            );
            messagingTemplate.convertAndSend("/topic/conversation/" + conversationId + "/read", readPayload);
        } catch (Exception e) {
            log.warn("Could not broadcast read receipt", e);
        }

        return response;
    }

    private Conversation resolveOrCreateConversation(ChatMessageRequest request) {
        if (request.getConversationId() != null) {
            Optional<Conversation> convOpt = conversationRepository.findById(request.getConversationId());
            if (convOpt.isPresent()) {
                return convOpt.get();
            }
        }

        final Integer resolvedMotherId = (request.getMotherId() != null)
                ? request.getMotherId()
                : ("MOTHER".equalsIgnoreCase(request.getSenderRole()) ? request.getSenderId() : null);

        if (resolvedMotherId != null) {
            Optional<Conversation> convOpt = conversationRepository.findByMotherId(resolvedMotherId);
            if (convOpt.isPresent()) {
                return convOpt.get();
            }

            Mother mother = motherRepository.findById(resolvedMotherId)
                    .or(() -> motherRepository.findByUser_UserId(resolvedMotherId))
                    .orElseThrow(() -> new IllegalArgumentException("Mother not found with ID or User ID: " + resolvedMotherId));

            String areaCode = mother.getPhmArea() != null ? mother.getPhmArea().getAreaCode() : request.getPhmAreaCode();
            Integer areaId = mother.getPhmArea() != null ? mother.getPhmArea().getPhmAreaId() : null;

            Conversation newConv = Conversation.builder()
                    .motherId(mother.getMotherId())
                    .motherName(mother.getName())
                    .phmAreaCode(areaCode != null ? areaCode : "01")
                    .phmAreaId(areaId)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .unreadCountForMidwife(0)
                    .unreadCountForMother(0)
                    .build();
            return conversationRepository.save(newConv);
        }

        throw new IllegalArgumentException("Cannot resolve conversation: motherId or valid conversationId is required");
    }

    private void validatePhmAreaAccess(Conversation conversation, Integer senderId, String senderRole) {
        if ("MIDWIFE".equals(senderRole)) {
            // Check if Midwife's assigned area matches the conversation's PHM area
            Optional<Midwife> midwifeOpt = midwifeRepository.findById(senderId)
                    .or(() -> midwifeRepository.findByUser_UserId(senderId));

            if (midwifeOpt.isPresent()) {
                Midwife midwife = midwifeOpt.get();
                if (midwife.getPhmArea() != null) {
                    Integer midwifeAreaId = midwife.getPhmArea().getPhmAreaId();
                    String midwifeAreaCode = midwife.getPhmArea().getAreaCode();

                    boolean areaMatches = (conversation.getPhmAreaId() != null && conversation.getPhmAreaId().equals(midwifeAreaId))
                            || (conversation.getPhmAreaCode() != null && conversation.getPhmAreaCode().equalsIgnoreCase(midwifeAreaCode));

                    if (!areaMatches) {
                        throw new SecurityException("PHM Area Isolation Violation: Midwife (Area: "
                                + midwifeAreaCode + ") is not authorized to interact with Mother in PHM Area: "
                                + conversation.getPhmAreaCode());
                    }
                }
            }
        }
    }
}
