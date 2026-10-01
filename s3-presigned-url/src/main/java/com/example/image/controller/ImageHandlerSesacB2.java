package com.example.image.controller;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.example.common.AbstractLambdaHandler;
import com.example.image.dto.ImageResponse;
import com.example.image.dto.ImageUploadRequest;
import com.example.image.service.ImageService;

import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

public class ImageHandlerSesacB2 extends AbstractLambdaHandler {

  private final ImageService imageService;

  public ImageHandlerSesacB2() {
    S3Client s3Client = S3Client.builder()
      .httpClient(UrlConnectionHttpClient.create())
      .region(Region.AP_NORTHEAST_2)
      .build();
    S3Presigner s3Presigner = S3Presigner.builder()
      .s3Client(s3Client)
      .build();
    this.imageService = new ImageService(s3Presigner);
  }

  @Override
  protected APIGatewayV2HTTPResponse processRequest(APIGatewayV2HTTPEvent input, Context context) throws Exception {
    String method = input.getRequestContext().getHttp().getMethod();

    if ("POST".equalsIgnoreCase(method)) {
      String body = input.getBody();
      if (body == null || body.isBlank()) {
        throw new IllegalArgumentException("요청 Body가 없습니다.");
      }

      ImageUploadRequest request = objectMapper.readValue(body, ImageUploadRequest.class);
      ImageResponse responseDto = imageService.createPresignedUrl(request.filename(), request.contentType());

      return response(200, responseDto);
    }

    return response(405, Map.of("error", "Method Not Allowed"));
  }
}