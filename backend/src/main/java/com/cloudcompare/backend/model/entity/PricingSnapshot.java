package com.cloudcompare.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "pricing_snapshots",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"provider", "service_name", "region", "instance_type", "snapshot_date"}
        ),
        indexes = {
                @Index(name = "idx_provider_service_region", columnList = "provider,service_type,region"),
                @Index(name = "idx_snapshot_date", columnList = "snapshot_date")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String provider; // AWS, AZURE, GCP

    @Column(name = "service_type", nullable = false, length = 50)
    private String serviceType; // COMPUTE, DATABASE, STORAGE, NETWORK

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName; // EC2, RDS, S3, Lambda, etc.

    @Column(nullable = false, length = 50)
    private String region;

    @Column(name = "instance_type", length = 100)
    private String instanceType; // t3.medium, Standard_D2s_v3, n1-standard-1

    @Column(name = "price_per_hour", precision = 10, scale = 4)
    private BigDecimal pricePerHour;

    @Column(name = "price_per_month", precision = 10, scale = 2)
    private BigDecimal pricePerMonth;

    @Column(length = 3)
    private String currency = "USD";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pricing_data", columnDefinition = "jsonb")
    private Map<String, Object> pricingData; // Full pricing details as JSON

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (snapshotDate == null) {
            snapshotDate = LocalDate.now();
        }
    }
}