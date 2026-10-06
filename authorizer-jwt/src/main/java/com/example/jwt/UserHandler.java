package com.example.jwt;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  // Record 응답 DTO 정의
  private record UserResponseDto(String userId) { }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent input, Context context) {
    String userId = "Unknown";

    if (input.getRequestContext() != null && input.getRequestContext().getAuthorizer() != null) {
      Map<String, Object> lambdaContext = input.getRequestContext().getAuthorizer().getLambda();
      if (lambdaContext != null) {
        userId = (String) lambdaContext.getOrDefault("principalId", "Unknown");
      }
    }

    try {
      UserResponseDto responseDto = new UserResponseDto(userId);
      String responseBody = objectMapper.writeValueAsString(responseDto);

      return APIGatewayV2HTTPResponse.builder()
          .withStatusCode(200)
          .withHeaders(Map.of("Content-Type", "application/json; charset=UTF-8"))
          .withBody(responseBody)
          .build();
    } catch (Exception e) {
      return APIGatewayV2HTTPResponse.builder()
          .withStatusCode(500)
          .withBody("{\"error\": \"서버 내부 직렬화 오류\"}")
          .build();
    }
  }
}