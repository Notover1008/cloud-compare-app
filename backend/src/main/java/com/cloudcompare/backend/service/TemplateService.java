package com.cloudcompare.backend.service;

import com.cloudcompare.backend.model.dto.TemplateResponse;
import com.cloudcompare.backend.model.entity.Template;
import com.cloudcompare.backend.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TemplateService {

    private final TemplateRepository templateRepository;

    @Cacheable("templates")
    public List<TemplateResponse> getAllActiveTemplates() {
        log.info("Fetching all active templates");
        return templateRepository.findByIsActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "templates", key = "#category")
    public List<TemplateResponse> getTemplatesByCategory(String category) {
        log.info("Fetching templates for category: {}", category);
        return templateRepository.findByCategoryAndIsActiveTrue(category)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TemplateResponse getTemplateById(Long id) {
        log.info("Fetching template by id: {}", id);
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));
        return toResponse(template);
    }

    private TemplateResponse toResponse(Template template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .category(template.getCategory())
                .templateConfig(template.getTemplateConfig())
                .isActive(template.getIsActive())
                .build();
    }
}