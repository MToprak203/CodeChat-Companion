package com.ai.assistant.external.minio.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties props) {
        return MinioClient.builder()
                .endpoint(props.getEndpoint())
                .credentials(props.getAccessKey(), props.getSecretKey())
                .build();
    }

    @Bean
    public String projectBucket(MinioClient client, MinioProperties props) throws Exception {
        boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(props.getBucket()).build());
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(props.getBucket()).build());
        }
        return props.getBucket();
    }
}
