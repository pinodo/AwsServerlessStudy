package com.example.s3.controller;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.example.common.AbstractLambdaHandler;
import com.example.s3.dto.FileResponse;
import com.example.s3.dto.FileUploadRequest;
import com.example.s3.service.S3Service;

import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class S3CrudHandlerSesacB2 extends AbstractLambdaHandler {

  private final S3Service s3Service;

  public S3CrudHandlerSesacB2() {
    S3Client s3Client = S3Client.builder()
      .httpClient(UrlConnectionHttpClient.create())
      .region(Region.AP_NORTHEAST_2)
      .build();
    this.s3Service = new S3Service(s3Client);
  }

  @Override
  protected APIGatewayV2HTTPResponse processRequest(APIGatewayV2HTTPEvent input, Context context) throws Exception {
    String method = input.getRequestContext().getHttp().getMethod();
    String path = input.getRequestContext().getHttp().getPath();

    // 1. 파일 업로드 (POST /api/s3/files)
    if ("POST".equalsIgnoreCase(method) && "/api/s3/files".equals(path)) {
      String body = input.getBody();
      if (body == null || body.isBlank()) {
        throw new IllegalArgumentException("요청 Body가 없습니다.");
      }
      FileUploadRequest req = objectMapper.readValue(body, FileUploadRequest.class);
      s3Service.uploadFile(req.key(), req.content());
      return response(201, Map.of("message", "파일 업로드 성공", "key", req.key()));
    }

    // 2. 파일 목록 조회 (GET /api/s3/files)
    if ("GET".equalsIgnoreCase(method) && "/api/s3/files".equals(path)) {
      Map<String, String> queryParams = input.getQueryStringParameters();
      String prefix = (queryParams != null) ? queryParams.get("prefix") : null;
      List<FileResponse> files = s3Service.listFiles(prefix);
      return response(200, files);
    }

    // 3. 파일 단건 내용 읽기 (GET /api/s3/files/{key+})
    if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/s3/files/")) {
      String fileKey = extractFileKey(path, "/api/s3/files/");
      String content = s3Service.getFileContent(fileKey);
      return response(200, Map.of("key", fileKey, "content", content));
    }

    // 4. 파일 삭제 (DELETE /api/s3/files/{key+})
    if ("DELETE".equalsIgnoreCase(method) && path.startsWith("/api/s3/files/")) {
      String fileKey = extractFileKey(path, "/api/s3/files/");
      s3Service.deleteFile(fileKey);
      return response(204, null);
    }

    return response(405, Map.of("error", "Method Not Allowed"));
  }

  private String extractFileKey(String path, String prefix) {
    String key = path.substring(prefix.length());
    if (key.isBlank()) {
      throw new IllegalArgumentException("파일 경로(Key)가 지정되지 않았습니다.");
    }
    return key;
  }
}