package com.example.s3.service;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.example.s3.dto.FileResponse;
import com.example.s3.exception.FileNotFoundException;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class S3Service {

  private final S3Client s3Client;
  private final String bucketName = System.getenv("BUCKET_NAME");

  public S3Service(S3Client s3Client) {
    this.s3Client = s3Client;
  }

  // 1. 파일 업로드 (Put)
  public void uploadFile(String key, String content) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("파일 키(key)는 필수입니다.");
    }
    PutObjectRequest putReq = PutObjectRequest.builder()
      .bucket(bucketName)
      .key(key)
      .build();

    byte[] bytes = (content != null ? content : "").getBytes(StandardCharsets.UTF_8);
    s3Client.putObject(putReq, RequestBody.fromBytes(bytes));
  }

  // 2. 파일 내용 읽기 (Get)
  public String getFileContent(String key) {
    try {
      GetObjectRequest getReq = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();

      return s3Client.getObjectAsBytes(getReq).asUtf8String();
    } catch (S3Exception e) {
      if (e.statusCode() == 404) {
        throw new FileNotFoundException("파일을 찾을 수 없습니다: " + key);
      }
      throw e;
    }
  }

  // 3. 파일 목록 조회 (List)
  public List<FileResponse> listFiles(String prefix) {
    ListObjectsV2Request.Builder listReqBuilder = ListObjectsV2Request.builder()
      .bucket(bucketName);

    if (prefix != null && !prefix.isBlank()) {
      listReqBuilder.prefix(prefix);
    }

    ListObjectsV2Response result = s3Client.listObjectsV2(listReqBuilder.build());

    return result.contents().stream()
      .map(obj -> new FileResponse(
          obj.key(),
          obj.size(),
          obj.lastModified() != null ? obj.lastModified().toString() : ""
      ))
      .toList();
  }

  // 4. 파일 삭제 (Delete)
  public void deleteFile(String key) {
    DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
      .bucket(bucketName)
      .key(key)
      .build();

    s3Client.deleteObject(deleteReq);
  }
}