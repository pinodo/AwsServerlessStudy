package com.example.lounge;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class LoungeHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  // 응답 메시지 생성을 위한 Record
  private record LoungeResponseDto(String message, String role) { }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent input, Context context) {
    String memberName = "Unknown";
    String role = "GUEST";

    // Lambda Custom Authorizer가 전달한 context 추출
    if (input.getRequestContext() != null && input.getRequestContext().getAuthorizer() != null) {
      Map<String, Object> lambdaContext = input.getRequestContext().getAuthorizer().getLambda();

      if (lambdaContext != null) {
        memberName = (String) lambdaContext.getOrDefault("memberName", "Unknown");
        role = (String) lambdaContext.getOrDefault("role", "GUEST");
      }
    }

    try {
      String welcomeMessage = String.format("%s님! 라운지에 오신 걸 환영합니다.", memberName);
      LoungeResponseDto responseDto = new LoungeResponseDto(welcomeMessage, role);
      String responseBody = objectMapper.writeValueAsString(responseDto);

      return APIGatewayV2HTTPResponse.builder()
          .withStatusCode(200)
          .withHeaders(Map.of("Content-Type", "application/json; charset=UTF-8"))
          .withBody(responseBody)
          .build();
    } catch (Exception e) {
      return APIGatewayV2HTTPResponse.builder()
          .withStatusCode(500)
          .withBody("{\"error\": \"서버 내부 처리 오류가 발생했습니다.\"}")
          .build();
    }
  }
}