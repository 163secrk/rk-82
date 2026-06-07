package com.freelance.platform.entity;

import com.freelance.platform.common.enums.DisputeStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "dispute")
public class Dispute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "project_id", nullable = false)
    private Integer projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    @Column(name = "initiator_id", nullable = false)
    private Integer initiatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", insertable = false, updatable = false)
    private User initiator;

    @Column(name = "respondent_id", nullable = false)
    private Integer respondentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondent_id", insertable = false, updatable = false)
    private User respondent;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "claimed_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal claimedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DisputeStatus status = DisputeStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "resolved_amount", precision = 10, scale = 2)
    private BigDecimal resolvedAmount;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "admin_id")
    private Integer adminId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", insertable = false, updatable = false)
    private User admin;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
