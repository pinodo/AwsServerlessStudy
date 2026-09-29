package com.example.user;

import java.time.Duration;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

public class ProfileHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>{

  private static final S3Client s3Client = S3Client.builder()
    .httpClientBuilder(UrlConnectionHttpClient.builder())
    .region(Region.AP_NORTHEAST_2)
    .build();
  private static final S3Presigner s3Presigner = S3Presigner.builder()
    .s3Client(s3Client)
    .build();

  private final String BUCKET_NAME = System.getenv("BUCKET_NAME");
  private final String KEY = "image1.jpg";

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

    // 요청 메서드
    String method = input.getHttpMethod();

    if (!method.equalsIgnoreCase("POST")) {
      return null;
    }

    // S3 기본 요청 정보
    PutObjectRequest objectRequest = PutObjectRequest.builder()
      .bucket(BUCKET_NAME)
      .key(KEY)
      .contentType("image/jpeg")
      .build();

    // Presigned URL 요청 정보
    PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
      .signatureDuration(Duration.ofMinutes(5))
      .putObjectRequest(objectRequest)
      .build();

    // 요청
    PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(presignRequest);
    String uploadUrl = presignedPutObjectRequest.url().toString();

    // 응답 만들어서 반환하기
    APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
    response.withStatusCode(200)
      .withHeaders(Map.of("Content-Type", "application/json; charset=UTF-8"))
      .withBody("{\"uploadUrl\": \"" + uploadUrl + "\"}");

    return response;
  }
}
