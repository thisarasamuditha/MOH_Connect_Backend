package com.moh.moh_backend.model;

import jakarta.persistence.*;
import lombok.*;
import com.moh.moh_backend.Enum.TargetAudience;

import java.time.LocalDateTime;

@Entity
@Table(name = "SESSION_TYPE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_type_id")
    private Integer sessionTypeId;

    @Column(name = "type_name", length = 255, unique = true, nullable = false)
    private String typeName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_audience")
    @Enumerated(EnumType.STRING)
    private TargetAudience targetAudience;


}
