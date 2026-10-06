package com.example.lounge;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2CustomAuthorizerEvent;

/**
 * HTTP API v2 Simple Authorizer Handler
 * 
 * - Input: APIGatewayV2CustomAuthorizerEvent
 * - Output: Map<String, Object> (HTTP API v2 Simple Response 규격: isAuthorized, context)
 */
public class LoungeAuthorizerHandler implements RequestHandler<APIGatewayV2CustomAuthorizerEvent, Map<String, Object>> {

  @Override
  public Map<String, Object> handleRequest(APIGatewayV2CustomAuthorizerEvent input, Context context) {
    // 요청 헤더 수신 (HTTP API v2 특성상 소문자로 정규화 됨)
    Map<String, String> headers = input.getHeaders();
    String token = null;

    if (headers != null) {
      token = headers.getOrDefault("authorization", headers.get("Authorization"));
    }

    context.getLogger().log("Authorization Header: " + token);

    // 검증 로직 ("GOLD-MEMBER" 토큰 매칭 시 통과)
    boolean isAuthorized = "GOLD-MEMBER".equals(token);

    return generateAuthorizerResponse("gold-member", isAuthorized);
  }

  /**
   * HTTP API v2 Simple Response 맵 생성
   * 
   * 반환 JSON 구조:
   * {
   *   "isAuthorized": true/false,
   *   "context": {
   *     "principalId": "...",
   *     "role": "...",
   *     "memberName": "..."
   *   }
   * }
   */
  private Map<String, Object> generateAuthorizerResponse(String principalId, boolean isAuthorized) {
    Map<String, Object> contextMap = new HashMap<>();
    contextMap.put("principalId", principalId);
    contextMap.put("role", "GOLD");
    contextMap.put("memberName", "gold-member");

    Map<String, Object> response = new HashMap<>();
    response.put("isAuthorized", isAuthorized);
    response.put("context", contextMap);

    return response;
  }
}