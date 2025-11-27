package com.cloudcompare.backend.repository;

import com.cloudcompare.backend.model.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {

    // Find active templates
    List<Template> findByIsActiveTrue();

    // Find by category
    List<Template> findByCategoryAndIsActiveTrue(String category);

    // Find by name
    Optional<Template> findByName(String name);
}