package com.wip.workipedia.config;

import com.wip.workipedia.storage.adapter.MinioStorageAdapter;
import com.wip.workipedia.storage.adapter.R2StorageAdapter;
import com.wip.workipedia.storage.adapter.S3StorageAdapter;
import com.wip.workipedia.storage.port.StoragePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "r2")
    public StoragePort r2StorageAdapter(StorageProperties props) {
        return new R2StorageAdapter(props);
    }

    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "minio")
    public StoragePort minioStorageAdapter(StorageProperties props) {
        return new MinioStorageAdapter(props);
    }

    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
    public StoragePort s3StorageAdapter(StorageProperties props) {
        return new S3StorageAdapter(props);
    }
}
