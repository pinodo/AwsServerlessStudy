package com.example.user;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class UserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  // DB 대신 Map 구조 사용
  private final Map<Integer, String> users = new HashMap<>();

  public UserHandler() {
    // 생성자에서 Mock Data 초기화
    users.put(100, "Junior Dev");
    users.put(200, "Senior Dev");
    users.put(300, "Project Manager");
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
    // Path Parameter 추출 (/api/users/{id})
    Map<String, String> pathParameters = input.getPathParameters();
    String strId = pathParameters != null ? pathParameters.get("id") : null;

    context.getLogger().log("Requested ID: " + strId);

    // 응답용 공통 헤더 설정 (JSON)
    Map<String, String> headers = Map.of("Content-Type", "application/json");

    try {
      if (strId == null) {
        return error(400, "Invalid Request: ID is missing", headers);
      }

      int targetId = Integer.parseInt(strId);
      
      // Map에서 조회
      String foundName = users.get(targetId);

      if (foundName != null) {
        // 200 OK + JSON 포맷팅
        String responseBody = String.format("{\"id\": %d, \"name\": \"%s\"}", targetId, foundName);
        return ok(responseBody, headers);
      } else {
        // 404 Not Found + "User not found"
        return error(404, "User not found", headers);
      }

    } catch (NumberFormatException e) {
      // ID가 정수형 숫자가 아닐 경우 400 에러 반환
      return error(400, "ID must be a number", headers);
    }
  }

  // 정상 응답 생성
  private APIGatewayProxyResponseEvent ok(String body, Map<String, String> headers) {
    return new APIGatewayProxyResponseEvent()
      .withStatusCode(200)
      .withHeaders(headers)
      .withBody(body);
  }

  // 에러 응답 생성
  private APIGatewayProxyResponseEvent error(int statusCode, String message, Map<String, String> headers) {
    return new APIGatewayProxyResponseEvent()
      .withStatusCode(statusCode)
      .withHeaders(headers)
      .withBody(String.format("{\"error\": \"%s\"}", message));
  }
}