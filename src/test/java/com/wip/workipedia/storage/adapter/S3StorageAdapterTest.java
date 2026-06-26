package com.wip.workipedia.storage.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.wip.workipedia.config.StorageProperties;
import com.wip.workipedia.config.StorageProperties.MinioProperties;
import com.wip.workipedia.config.StorageProperties.R2Properties;
import com.wip.workipedia.config.StorageProperties.S3Properties;
import com.wip.workipedia.storage.dto.PresignedUploadRequest;
import com.wip.workipedia.storage.dto.PresignedUploadResponse;
import org.junit.jupiter.api.Test;

class S3StorageAdapterTest {

    private S3StorageAdapter adapterWithS3PublicUrl(String s3PublicUrl) {
        StorageProperties props = new StorageProperties(
            "s3",
            "shared-r2-bucket",
            new R2Properties("rk", "rs", "acc", "https://leaked-r2.r2.dev/"),
            new MinioProperties("mk", "ms", "http://minio:9000", "http://minio-public/"),
            new S3Properties("ak", "sk", "ap-northeast-2", "my-s3-bucket", s3PublicUrl)
        );
        return new S3StorageAdapter(props);
    }

    @Test
    void publicUrl_ignores_other_providers_and_auto_composes_when_s3_public_url_blank() {
        PresignedUploadResponse res = adapterWithS3PublicUrl("")
            .createPresignedUploadUrl(new PresignedUploadRequest("a.pdf", "application/pdf"));

        assertThat(res.publicUrl())
            .startsWith("https://my-s3-bucket.s3.ap-northeast-2.amazonaws.com/")
            .doesNotContain("r2.dev");
    }

    @Test
    void publicUrl_uses_s3_specific_public_url_when_set() {
        PresignedUploadResponse res = adapterWithS3PublicUrl("https://cdn.example.com/")
            .createPresignedUploadUrl(new PresignedUploadRequest("a.pdf", "application/pdf"));

        assertThat(res.publicUrl()).startsWith("https://cdn.example.com/");
    }
}
