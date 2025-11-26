package com.cloudcompare;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.amazon.awscdk.services.secretsmanager.SecretStringGenerator;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class CloudCompareStack extends Stack {

    public CloudCompareStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // ============================================
        // VPC and Networking
        // ============================================

        Vpc vpc = Vpc.Builder.create(this, "CloudCompareVpc")
                .vpcName("cloud-compare-vpc")
                .maxAzs(2)
                .natGateways(0)  // No NAT Gateway to save costs
                .subnetConfiguration(java.util.List.of(
                        SubnetConfiguration.builder()
                                .name("Public")
                                .subnetType(SubnetType.PUBLIC)
                                .cidrMask(24)
                                .build(),
                        SubnetConfiguration.builder()
                                .name("Private")
                                .subnetType(SubnetType.PRIVATE_ISOLATED)
                                .cidrMask(24)
                                .build()
                ))
                .build();

        // VPC Endpoints for AWS services
        vpc.addInterfaceEndpoint("EcrDockerEndpoint", InterfaceVpcEndpointOptions.builder()
                .service(InterfaceVpcEndpointAwsService.ECR_DOCKER)
                .build());

        vpc.addInterfaceEndpoint("EcrEndpoint", InterfaceVpcEndpointOptions.builder()
                .service(InterfaceVpcEndpointAwsService.ECR)
                .build());

        vpc.addInterfaceEndpoint("CloudWatchLogsEndpoint", InterfaceVpcEndpointOptions.builder()
                .service(InterfaceVpcEndpointAwsService.CLOUDWATCH_LOGS)
                .build());

        vpc.addGatewayEndpoint("S3Endpoint", GatewayVpcEndpointOptions.builder()
                .service(GatewayVpcEndpointAwsService.S3)
                .build());

        // ============================================
        // Database Security Group
        // ============================================

        SecurityGroup databaseSecurityGroup = SecurityGroup.Builder.create(this, "DatabaseSecurityGroup")
                .vpc(vpc)
                .description("Security group for Aurora PostgreSQL database")
                .allowAllOutbound(false)
                .build();

        // ============================================
        // Database Credentials
        // ============================================

        Secret databaseCredentials = Secret.Builder.create(this, "DatabaseCredentials")
                .secretName("cloud-compare-db-credentials")
                .description("Database credentials for Cloud Compare App")
                .generateSecretString(SecretStringGenerator.builder()
                        .secretStringTemplate("{\"username\":\"cloudcompare\"}")
                        .generateStringKey("password")
                        .excludePunctuation(true)
                        .passwordLength(32)
                        .build())
                .build();

        // ============================================
        // Aurora PostgreSQL Serverless v2
        // ============================================

        DatabaseCluster databaseCluster = DatabaseCluster.Builder.create(this, "DatabaseCluster")
                .clusterIdentifier("cloud-compare-db")
                .engine(DatabaseClusterEngine.auroraPostgres(AuroraPostgresClusterEngineProps.builder()
                        .version(AuroraPostgresEngineVersion.VER_15_5)
                        .build()))
                .credentials(Credentials.fromSecret(databaseCredentials))
                .defaultDatabaseName("cloudcompare")
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_ISOLATED)
                        .build())
                .securityGroups(java.util.List.of(databaseSecurityGroup))
                .serverlessV2MinCapacity(0.5)
                .serverlessV2MaxCapacity(2.0)
                .writer(ClusterInstance.serverlessV2("writer"))
                .backup(BackupProps.builder()
                        .retention(Duration.days(7))
                        .preferredWindow("03:00-04:00")
                        .build())
                .removalPolicy(RemovalPolicy.SNAPSHOT)
                .storageEncrypted(true)
                .build();

        // ============================================
        // ECR Repository
        // ============================================

        Repository ecrRepository = Repository.Builder.create(this, "BackendRepository")
                .repositoryName("cloud-compare-backend")
                .build();

        // ============================================
        // ECS Cluster
        // ============================================

        Cluster cluster = Cluster.Builder.create(this, "EcsCluster")
                .clusterName("cloud-compare-cluster")
                .vpc(vpc)
                .containerInsights(true)
                .build();

        // ============================================
        // CloudWatch Log Group
        // ============================================

        LogGroup logGroup = LogGroup.Builder.create(this, "ApplicationLogGroup")
                .logGroupName("/ecs/cloud-compare-backend")
                .retention(RetentionDays.ONE_WEEK)
                .build();

        // ============================================
        // Environment Variables for Spring Boot
        // ============================================

        Map<String, String> environment = new HashMap<>();
        environment.put("SPRING_PROFILES_ACTIVE", "prod");
        environment.put("SERVER_PORT", "8080");
        environment.put("SPRING_DATASOURCE_URL",
                "jdbc:postgresql://" + databaseCluster.getClusterEndpoint().getHostname() +
                        ":5432/cloudcompare");

        // ============================================
        // ECS Fargate Service with ALB
        // ============================================

        ApplicationLoadBalancedFargateService fargateService =
                ApplicationLoadBalancedFargateService.Builder.create(this, "FargateService")
                        .serviceName("cloud-compare-service")
                        .cluster(cluster)
                        .cpu(512)
                        .memoryLimitMiB(1024)
                        .desiredCount(1)
                        .taskImageOptions(ApplicationLoadBalancedTaskImageOptions.builder()
                                .containerName("cloud-compare-backend")
                                .image(ContainerImage.fromEcrRepository(ecrRepository, "latest"))
                                .containerPort(8080)
                                .environment(environment)
                                .secrets(Map.of(
                                        "SPRING_DATASOURCE_USERNAME",
                                        software.amazon.awscdk.services.ecs.Secret.fromSecretsManager(databaseCredentials, "username"),
                                        "SPRING_DATASOURCE_PASSWORD",
                                        software.amazon.awscdk.services.ecs.Secret.fromSecretsManager(databaseCredentials, "password")
                                ))
                                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                        .streamPrefix("backend")
                                        .logGroup(logGroup)
                                        .build()))
                                .build())
                        .publicLoadBalancer(true)
                        .assignPublicIp(true)
                        .taskSubnets(SubnetSelection.builder()
                                .subnetType(SubnetType.PUBLIC)
                                .build())
                        .build();

        // ============================================
        // Health Check Configuration
        // ============================================

        fargateService.getTargetGroup().configureHealthCheck(HealthCheck.builder()
                .path("/actuator/health")
                .interval(Duration.seconds(60))
                .timeout(Duration.seconds(5))
                .healthyThresholdCount(2)
                .unhealthyThresholdCount(3)
                .build());

        // ============================================
        // Security Group Rules
        // ============================================

        // Allow ECS tasks to connect to database
        databaseSecurityGroup.addIngressRule(
                fargateService.getService().getConnections().getSecurityGroups().get(0),
                Port.tcp(5432),
                "Allow ECS tasks to connect to database"
        );

        // ============================================
        // IAM Permissions
        // ============================================

        ecrRepository.grantPull(fargateService.getTaskDefinition().getExecutionRole());

        // ============================================
        // Auto-scaling
        // ============================================

        ScalableTaskCount scalableTaskCount = fargateService.getService().autoScaleTaskCount(
                EnableScalingProps.builder()
                        .minCapacity(1)
                        .maxCapacity(4)
                        .build()
        );

        scalableTaskCount.scaleOnCpuUtilization("CpuScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(70)
                .scaleInCooldown(Duration.seconds(60))
                .scaleOutCooldown(Duration.seconds(60))
                .build());
    }
}