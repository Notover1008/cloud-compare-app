package com.cloudcompare.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingRequest {

    @NotNull(message = "Template ID is required")
    private Long templateId;

    @NotBlank(message = "Region is required")
    private String region;

    @NotNull(message = "Configuration is required")
    private Map<String, Object> configuration; // User's resource selections

    // Example configuration:
    // {
    //   "compute": {"vcpus": 2, "memory": 4, "instanceType": "general"},
    //   "database": {"engine": "postgres", "storage": 100},
    //   "storage": {"sizeGB": 1000}
    // }
}