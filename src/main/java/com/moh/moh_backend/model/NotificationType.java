package com.moh.moh_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "NOTIFICATION_TYPE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Integer typeId;

    @Column(name = "type_name", length = 255, unique = true, nullable = false)
    private String typeName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "template", columnDefinition = "TEXT")
    private String template;

    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private Priority priority;

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
}
