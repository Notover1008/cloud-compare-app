package com.cloudcompare.backend.service;

import com.cloudcompare.backend.model.dto.PricingRequest;
import com.cloudcompare.backend.model.dto.PricingResponse;
import com.cloudcompare.backend.model.entity.Calculation;
import com.cloudcompare.backend.model.entity.Template;
import com.cloudcompare.backend.repository.CalculationRepository;
import com.cloudcompare.backend.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalculationService {

    private final CalculationRepository calculationRepository;
    private final TemplateRepository templateRepository;
    private final PricingService pricingService;

    @Transactional
    public PricingResponse calculateCosts(PricingRequest request, String ipAddress, String userAgent) {
        log.info("Calculating costs for template {} in region {}", request.getTemplateId(), request.getRegion());

        Template template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new RuntimeException("Template not found"));

        BigDecimal awsCost = calculateCloudCost("AWS", request);
        BigDecimal azureCost = calculateCloudCost("AZURE", request);
        BigDecimal gcpCost = calculateCloudCost("GCP", request);

        Calculation calculation = Calculation.builder()
                .template(template)
                .userConfig(request.getConfiguration())
                .awsCost(awsCost)
                .azureCost(azureCost)
                .gcpCost(gcpCost)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        calculation = calculationRepository.save(calculation);

        String cheapestProvider = determineCheapestProvider(awsCost, azureCost, gcpCost);
        BigDecimal maxSavings = calculateMaxSavings(awsCost, azureCost, gcpCost);

        return PricingResponse.builder()
                .calculationId(calculation.getId())
                .aws(buildCloudCost(awsCost))
                .azure(buildCloudCost(azureCost))
                .gcp(buildCloudCost(gcpCost))
                .cheapestProvider(cheapestProvider)
                .maxSavings(maxSavings)
                .region(request.getRegion())
                .build();
    }

    private BigDecimal calculateCloudCost(String provider, PricingRequest request) {
        return BigDecimal.valueOf(100.00);
    }

    private PricingResponse.CloudCost buildCloudCost(BigDecimal totalCost) {
        Map<String, BigDecimal> breakdown = new HashMap<>();
        breakdown.put("compute", totalCost.multiply(BigDecimal.valueOf(0.5)));
        breakdown.put("database", totalCost.multiply(BigDecimal.valueOf(0.3)));
        breakdown.put("storage", totalCost.multiply(BigDecimal.valueOf(0.2)));

        return PricingResponse.CloudCost.builder()
                .totalMonthlyCost(totalCost)
                .breakdown(breakdown)
                .currency("USD")
                .build();
    }

    private String determineCheapestProvider(BigDecimal aws, BigDecimal azure, BigDecimal gcp) {
        if (aws.compareTo(azure) <= 0 && aws.compareTo(gcp) <= 0) {
            return "AWS";
        } else if (azure.compareTo(gcp) <= 0) {
            return "AZURE";
        } else {
            return "GCP";
        }
    }

    private BigDecimal calculateMaxSavings(BigDecimal aws, BigDecimal azure, BigDecimal gcp) {
        BigDecimal max = aws.max(azure).max(gcp);
        BigDecimal min = aws.min(azure).min(gcp);
        return max.subtract(min);
    }
}