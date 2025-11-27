package com.cloudcompare.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingResponse {

    private Long calculationId;

    private CloudCost aws;
    private CloudCost azure;
    private CloudCost gcp;

    private String cheapestProvider;
    private BigDecimal maxSavings;
    private String region;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CloudCost {
        private BigDecimal totalMonthlyCost;
        private Map<String, BigDecimal> breakdown; // Service-level breakdown
        private String currency;
    }
}