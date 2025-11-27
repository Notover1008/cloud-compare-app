package com.cloudcompare.backend.service;

import com.cloudcompare.backend.model.entity.PricingSnapshot;
import com.cloudcompare.backend.repository.PricingSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PricingService {

    private final PricingSnapshotRepository pricingSnapshotRepository;

    @Cacheable(value = "pricing", key = "#provider + '-' + #serviceName + '-' + #region + '-' + #instanceType")
    public Optional<PricingSnapshot> getLatestPricing(String provider, String serviceName,
                                                      String region, String instanceType) {
        log.debug("Fetching latest pricing for {}/{}/{}/{}", provider, serviceName, region, instanceType);
        return pricingSnapshotRepository
                .findFirstByProviderAndServiceNameAndRegionAndInstanceTypeOrderBySnapshotDateDesc(
                        provider, serviceName, region, instanceType);
    }

    public List<PricingSnapshot> getPricingByServiceType(String provider, String serviceType,
                                                         String region, LocalDate date) {
        log.debug("Fetching pricing for {}/{}/{} on {}", provider, serviceType, region, date);
        return pricingSnapshotRepository.findByProviderAndServiceTypeAndRegionAndSnapshotDate(
                provider, serviceType, region, date);
    }

    public boolean hasPricingForToday(String provider) {
        return pricingSnapshotRepository.existsByProviderAndSnapshotDate(provider, LocalDate.now());
    }

    @Transactional
    public PricingSnapshot savePricingSnapshot(PricingSnapshot snapshot) {
        log.info("Saving pricing snapshot for {}/{}/{}",
                snapshot.getProvider(), snapshot.getServiceName(), snapshot.getRegion());
        return pricingSnapshotRepository.save(snapshot);
    }

    @Transactional
    public void savePricingSnapshots(List<PricingSnapshot> snapshots) {
        log.info("Saving {} pricing snapshots", snapshots.size());
        pricingSnapshotRepository.saveAll(snapshots);
    }

    public BigDecimal calculateMonthlyCost(BigDecimal hourlyRate) {
        if (hourlyRate == null) {
            return BigDecimal.ZERO;
        }
        return hourlyRate.multiply(BigDecimal.valueOf(730));
    }
}