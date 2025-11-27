package com.cloudcompare.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "calculations",
        indexes = {
                @Index(name = "idx_calculation_date", columnList = "calculation_date"),
                @Index(name = "idx_template_id", columnList = "template_id")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Calculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private Template template;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "user_config", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> userConfig; // User's resource selections

    @Column(name = "aws_cost", precision = 10, scale = 2)
    private BigDecimal awsCost;

    @Column(name = "azure_cost", precision = 10, scale = 2)
    private BigDecimal azureCost;

    @Column(name = "gcp_cost", precision = 10, scale = 2)
    private BigDecimal gcpCost;

    @Column(name = "calculation_date", nullable = false)
    private LocalDateTime calculationDate;

    @Column(name = "ip_address", length = 45)
    private String ipAddress; // For analytics (IPv4 or IPv6)

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @PrePersist
    protected void onCreate() {
        calculationDate = LocalDateTime.now();
    }
}