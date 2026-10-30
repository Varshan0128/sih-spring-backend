package org.aicte.approval.core.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ai_risk_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel;

    @Column(name = "delay_probability", nullable = false, precision = 5, scale = 2)
    private BigDecimal delayProbability;

    @Column(name = "primary_bottleneck", columnDefinition = "TEXT")
    private String primaryBottleneck;

    @Column(name = "contributing_factors", columnDefinition = "TEXT")
    private String contributingFactors;

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @Builder.Default
    @Column(name = "assessment_timestamp", updatable = false)
    private Instant assessmentTimestamp = Instant.now();
}
