package com.gntcyouthbe.file.service;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
@Profile("!test & !local")
public class S3FileStorageService implements FileStorageService {

    private final S3Presigner s3Presigner;
    private final String bucket;
    private final Duration expirationDuration;

    public S3FileStorageService(
            S3Presigner s3Presigner,
            @Value("${aws.s3.bucket}") String bucket,
            @Value("${aws.s3.presigned-url-expiration-minutes}") long expirationMinutes) {
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
        this.expirationDuration = Duration.ofMinutes(expirationMinutes);
    }

    @Override
    public String generatePresignedUrl(String storedFilename, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key("uploads/" + storedFilename)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(expirationDuration)
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }
}
