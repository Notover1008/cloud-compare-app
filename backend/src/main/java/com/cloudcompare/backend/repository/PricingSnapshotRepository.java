package com.cloudcompare.backend.repository;

import com.cloudcompare.backend.model.entity.PricingSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PricingSnapshotRepository extends JpaRepository<PricingSnapshot, Long> {

    // Find latest pricing for a specific service
    Optional<PricingSnapshot> findFirstByProviderAndServiceNameAndRegionAndInstanceTypeOrderBySnapshotDateDesc(
            String provider, String serviceName, String region, String instanceType);

    // Find all pricing for a service type in a region
    List<PricingSnapshot> findByProviderAndServiceTypeAndRegionAndSnapshotDate(
            String provider, String serviceType, String region, LocalDate snapshotDate);

    // Find latest snapshots for all providers for comparison
    @Query("SELECT p FROM PricingSnapshot p WHERE p.snapshotDate = " +
            "(SELECT MAX(p2.snapshotDate) FROM PricingSnapshot p2 WHERE " +
            "p2.provider = p.provider AND p2.serviceName = p.serviceName AND " +
            "p2.region = p.region AND p2.instanceType = p.instanceType)")
    List<PricingSnapshot> findLatestSnapshots();

    // Check if pricing exists for today
    boolean existsByProviderAndSnapshotDate(String provider, LocalDate snapshotDate);
}