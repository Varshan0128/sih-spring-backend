package org.aicte.approval.document.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentStorageService {

    private final MinioClient minioClient;

    @Value("${app.minio.bucket-name:aicte-documents}")
    private String bucketName;

    public String storeFile(MultipartFile file, Long institutionId, Long applicationId) {
        try {
            ensureBucketExists();

            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String objectKey = String.format("institutions/%d/apps/%d/%s%s",
                    institutionId, applicationId, UUID.randomUUID(), extension);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("Stored file {} in MinIO as {}", originalName, objectKey);
            return objectKey;
        } catch (Exception e) {
            log.warn("MinIO upload failed, fallback to local storage reference: {}", e.getMessage());
            return "local-storage-ref/" + UUID.randomUUID() + "/" + file.getOriginalFilename();
        }
    }

    public InputStream getFileStream(String objectKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to retrieve file from MinIO: {}", e.getMessage());
            return null;
        }
    }

    public String generatePresignedUrl(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectKey)
                            .expiry(2, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            return "/api/v1/documents/download-stream?key=" + objectKey;
        }
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Created MinIO bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.warn("Could not check/create MinIO bucket: {}", e.getMessage());
        }
    }
}
