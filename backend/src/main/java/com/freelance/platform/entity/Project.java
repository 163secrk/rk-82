package com.freelance.platform.entity;

import com.freelance.platform.common.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "project")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String category;

    @Column(name = "budget_min", nullable = false, precision = 10, scale = 2)
    private BigDecimal budgetMin;

    @Column(name = "budget_max", nullable = false, precision = 10, scale = 2)
    private BigDecimal budgetMax;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.DRAFT;

    @Column(name = "client_id", nullable = false)
    private Integer clientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private User client;

    @Column(name = "freelancer_id")
    private Integer freelancerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelancer_id", insertable = false, updatable = false)
    private User freelancer;

    @Column(name = "agreed_price", precision = 10, scale = 2)
    private BigDecimal agreedPrice;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
