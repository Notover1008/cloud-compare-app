package com.cloudcompare.backend.repository;

import com.cloudcompare.backend.model.entity.Calculation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CalculationRepository extends JpaRepository<Calculation, Long> {

    // Count calculations in a time range
    long countByCalculationDateBetween(LocalDateTime start, LocalDateTime end);

    // Find recent calculations for analytics
    List<Calculation> findTop100ByOrderByCalculationDateDesc();

    // Get popular templates
    @Query("SELECT c.template.id, COUNT(c) as count FROM Calculation c " +
            "WHERE c.calculationDate >= :since " +
            "GROUP BY c.template.id ORDER BY count DESC")
    List<Object[]> findPopularTemplatesSince(LocalDateTime since);
}