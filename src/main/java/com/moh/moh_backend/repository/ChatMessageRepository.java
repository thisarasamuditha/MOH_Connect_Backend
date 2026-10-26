package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversation_ConversationIdOrderByTimestampAsc(Long conversationId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.conversation.conversationId = :conversationId AND m.senderRole = :senderRole AND m.isRead = false")
    int markMessagesAsReadForRole(@Param("conversationId") Long conversationId, @Param("senderRole") String senderRole);
}
