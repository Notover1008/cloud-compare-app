package com.cloudcompare.backend.controller;

import com.cloudcompare.backend.model.dto.PricingRequest;
import com.cloudcompare.backend.model.dto.PricingResponse;
import com.cloudcompare.backend.service.CalculationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final CalculationService calculationService;

    @PostMapping("/compare")
    public ResponseEntity<PricingResponse> comparePricing(
            @Valid @RequestBody PricingRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        PricingResponse response = calculationService.calculateCosts(request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}