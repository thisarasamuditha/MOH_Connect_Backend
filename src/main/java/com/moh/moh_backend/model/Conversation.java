package com.moh.moh_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id")
    private Long conversationId;

    @Column(name = "mother_id", nullable = false, unique = true)
    private Integer motherId;

    @Column(name = "mother_name", nullable = false, length = 255)
    private String motherName;

    @Column(name = "phm_area_code", nullable = false, length = 50)
    private String phmAreaCode;

    @Column(name = "phm_area_id")
    private Integer phmAreaId;

    @Column(name = "last_message", columnDefinition = "TEXT")
    private String lastMessage;

    @Column(name = "last_message_urgent")
    @Builder.Default
    private Boolean lastMessageUrgent = false;

    @Column(name = "unread_count_for_midwife")
    @Builder.Default
    private Integer unreadCountForMidwife = 0;

    @Column(name = "unread_count_for_mother")
    @Builder.Default
    private Integer unreadCountForMother = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("timestamp ASC")
    @JsonIgnore
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (lastMessageUrgent == null) lastMessageUrgent = false;
        if (unreadCountForMidwife == null) unreadCountForMidwife = 0;
        if (unreadCountForMother == null) unreadCountForMother = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
