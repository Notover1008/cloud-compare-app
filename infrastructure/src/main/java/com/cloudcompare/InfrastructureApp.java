package com.cloudcompare;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class InfrastructureApp {
    public static void main(final String[] args) {
        App app = new App();

        Environment env = Environment.builder()
                .account("539247462486")
                .region("us-east-1")
                .build();

        new CloudCompareStack(app, "CloudCompareStack", StackProps.builder()
                .env(env)
                .description("Complete infrastructure for Cloud Compare App")
                .build());

        app.synth();
    }
}