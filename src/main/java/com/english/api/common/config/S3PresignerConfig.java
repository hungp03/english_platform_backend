package com.english.api.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Created by hungpham on 9/29/2025
 */
@Configuration
@ConditionalOnProperty(name = "cloud.signing.enabled", havingValue = "true", matchIfMissing = false)
public class S3PresignerConfig {

    @Value("${cloud.endpoint}")
    private String endpoint;

    @Value("${cloud.accessKey}")
    private String accessKey;

    @Value("${cloud.secretKey}")
    private String secretKey;

    @Value("${cloud.region:ap-southeast-1}")
    private String region;

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .build();
    }
}
