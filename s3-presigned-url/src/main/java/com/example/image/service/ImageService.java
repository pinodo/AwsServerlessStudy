package com.example.image.service;

import java.time.Duration;
import java.util.UUID;

import com.example.image.dto.ImageResponse;

import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

public class ImageService {

  private final S3Presigner s3Presigner;
  private final String bucketName = System.getenv("BUCKET_NAME");

  public ImageService(S3Presigner s3Presigner) {
    this.s3Presigner = s3Presigner;
  }

  public ImageResponse createPresignedUrl(String filename, String contentType) {
    String ext = "";
    if (filename != null && filename.contains(".")) {
      ext = filename.substring(filename.lastIndexOf("."));
    }
    String imageKey = "raw/" + UUID.randomUUID().toString() + ext;

    PutObjectRequest objectRequest = PutObjectRequest.builder()
      .bucket(bucketName)
      .key(imageKey)
      .contentType(contentType != null ? contentType : "application/octet-stream")
      .build();

    PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(r -> r
      .signatureDuration(Duration.ofMinutes(10))
      .putObjectRequest(objectRequest)
    );

    return new ImageResponse(presignedRequest.url().toString(), imageKey);
  }
}