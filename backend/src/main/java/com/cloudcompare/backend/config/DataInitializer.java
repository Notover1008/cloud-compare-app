package com.cloudcompare.backend.config;

import com.cloudcompare.backend.model.entity.Template;
import com.cloudcompare.backend.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TemplateRepository templateRepository;

    @Override
    public void run(String... args) {
        if (templateRepository.count() == 0) {
            log.info("Initializing sample templates...");
            createSampleTemplates();
            log.info("Sample templates created successfully");
        } else {
            log.info("Templates already exist, skipping initialization");
        }
    }

    private void createSampleTemplates() {
        templateRepository.save(createWebApplicationTemplate());
        templateRepository.save(createDataPipelineTemplate());
        templateRepository.save(createServerlessApiTemplate());
        templateRepository.save(createMlInferenceTemplate());
    }

    private Template createWebApplicationTemplate() {
        Map<String, Object> config = new HashMap<>();

        Map<String, Object> compute = new HashMap<>();
        compute.put("type", "vm");
        compute.put("defaultVcpus", 2);
        compute.put("defaultMemoryGb", 4);
        compute.put("minVcpus", 1);
        compute.put("maxVcpus", 16);
        compute.put("minMemoryGb", 2);
        compute.put("maxMemoryGb", 64);
        config.put("compute", compute);

        Map<String, Object> database = new HashMap<>();
        database.put("engine", "postgres");
        database.put("defaultStorageGb", 100);
        database.put("minStorageGb", 20);
        database.put("maxStorageGb", 1000);
        config.put("database", database);

        Map<String, Object> storage = new HashMap<>();
        storage.put("defaultSizeGb", 500);
        storage.put("minSizeGb", 10);
        storage.put("maxSizeGb", 10000);
        config.put("storage", storage);

        config.put("loadBalancer", true);
        config.put("cdn", true);

        return Template.builder()
                .name("Web Application")
                .description("Scalable web application with load balancer, compute instances, managed database, object storage, and CDN")
                .category("WEB_APP")
                .templateConfig(config)
                .isActive(true)
                .build();
    }

    private Template createDataPipelineTemplate() {
        Map<String, Object> config = new HashMap<>();

        Map<String, Object> storage = new HashMap<>();
        storage.put("defaultSizeGb", 1000);
        storage.put("minSizeGb", 100);
        storage.put("maxSizeGb", 100000);
        config.put("storage", storage);

        Map<String, Object> etl = new HashMap<>();
        etl.put("service", "managed_etl");
        etl.put("defaultWorkers", 2);
        etl.put("minWorkers", 1);
        etl.put("maxWorkers", 10);
        config.put("etl", etl);

        Map<String, Object> dataWarehouse = new HashMap<>();
        dataWarehouse.put("defaultStorageGb", 500);
        dataWarehouse.put("minStorageGb", 100);
        dataWarehouse.put("maxStorageGb", 10000);
        config.put("dataWarehouse", dataWarehouse);

        return Template.builder()
                .name("Data Pipeline")
                .description("Complete data pipeline with object storage, ETL processing, and data warehouse for analytics")
                .category("DATA_PIPELINE")
                .templateConfig(config)
                .isActive(true)
                .build();
    }

    private Template createServerlessApiTemplate() {
        Map<String, Object> config = new HashMap<>();

        Map<String, Object> functions = new HashMap<>();
        functions.put("defaultMemoryMb", 512);
        functions.put("minMemoryMb", 128);
        functions.put("maxMemoryMb", 10240);
        functions.put("estimatedRequestsPerMonth", 1000000);
        config.put("functions", functions);

        Map<String, Object> apiGateway = new HashMap<>();
        apiGateway.put("enabled", true);
        apiGateway.put("estimatedRequestsPerMonth", 1000000);
        config.put("apiGateway", apiGateway);

        Map<String, Object> database = new HashMap<>();
        database.put("type", "nosql");
        database.put("defaultStorageGb", 25);
        config.put("database", database);

        return Template.builder()
                .name("Serverless API")
                .description("Serverless REST API with functions, API gateway, and NoSQL database")
                .category("SERVERLESS")
                .templateConfig(config)
                .isActive(true)
                .build();
    }

    private Template createMlInferenceTemplate() {
        Map<String, Object> config = new HashMap<>();

        Map<String, Object> compute = new HashMap<>();
        compute.put("type", "gpu");
        compute.put("defaultGpus", 1);
        compute.put("minGpus", 1);
        compute.put("maxGpus", 8);
        compute.put("defaultVcpus", 4);
        compute.put("defaultMemoryGb", 16);
        config.put("compute", compute);

        Map<String, Object> storage = new HashMap<>();
        storage.put("defaultSizeGb", 500);
        storage.put("minSizeGb", 100);
        storage.put("maxSizeGb", 5000);
        config.put("storage", storage);

        Map<String, Object> endpoint = new HashMap<>();
        endpoint.put("enabled", true);
        endpoint.put("autoScaling", true);
        config.put("endpoint", endpoint);

        return Template.builder()
                .name("ML Inference")
                .description("Machine learning model inference with GPU instances, model storage, and managed endpoints")
                .category("ML_INFERENCE")
                .templateConfig(config)
                .isActive(true)
                .build();
    }
}