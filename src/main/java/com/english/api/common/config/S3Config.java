package com.english.api.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${cloud.endpoint}")
    private String endpoint;

    @Value("${cloud.region:ap-southeast-1}")
    private String region;

    @Value("${cloud.accessKey}")
    private String accessKey;

    @Value("${cloud.secretKey}")
    private String secretKey;

    @Bean
    public S3AsyncClient s3AsyncClient() {
        return S3AsyncClient.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .httpClientBuilder(NettyNioAsyncHttpClient.builder()
                        .maxConcurrency(16)          // max connection
                        .maxPendingConnectionAcquires(100)
                )
                .build();
    }
}
