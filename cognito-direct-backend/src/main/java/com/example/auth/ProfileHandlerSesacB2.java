package com.example.auth;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProfileHandlerSesacB2 implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  // 응답용 Record
  private record ProfileResponseDto(String message, String userId) { }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent input, Context context) {
    String userId = "Unknown";

    // RequestContext -> Authorizer -> JWT -> Claims 순으로 안전하게 추출
    if (input.getRequestContext() != null && input.getRequestContext().getAuthorizer() != null) {
      APIGatewayV2HTTPEvent.RequestContext.Authorizer authorizer = input.getRequestContext().getAuthorizer();

      if (authorizer.getJwt() != null && authorizer.getJwt().getClaims() != null) {
        // Map<String, String> 타입으로 선언 및 sub 추출
        Map<String, String> jwtClaims = authorizer.getJwt().getClaims();
        userId = jwtClaims.getOrDefault("sub", "Unknown");
      }
    }

    try {
      ProfileResponseDto responseDto = new ProfileResponseDto("인증 성공!", userId);
      String responseBody = objectMapper.writeValueAsString(responseDto);

      return APIGatewayV2HTTPResponse.builder()
          .withStatusCode(200)
          .withHeaders(Map.of("Content-Type", "application/json; charset=UTF-8"))
          .withBody(responseBody)
          .build();
    } catch (Exception e) {
      return APIGatewayV2HTTPResponse.builder()
          .withStatusCode(500)
          .withBody("{\"error\": \"응답 생성 실패\"}")
          .build();
    }
  }
}