package com.example.jwt;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2CustomAuthorizerEvent;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtAuthorizerHandler implements RequestHandler<APIGatewayV2CustomAuthorizerEvent, Map<String, Object>> {

  // 단순 평문 사용 가능
  // private static final String SECRET_KEY = "this-is-my-super-secret-key-for-jwt-example";

  // Base64 인코딩 결과 (실무적 권장) / 명령어: echo -n "this-is-my-super-secret-key-for-jwt-example" | base64
  private static final String SECRET_KEY = "dGhpcy1pcy1teS1zdXBlci1zZWNyZXQta2V5LWZvci1qd3QtZXhhbXBsZQ==";

  @Override
  public Map<String, Object> handleRequest(APIGatewayV2CustomAuthorizerEvent input, Context context) {
    // HTTP API v2는 헤더 키를 소문자로 정규화 함
    Map<String, String> headers = input.getHeaders();
    String token = null;

    if (headers != null) {
      token = headers.getOrDefault("authorization", headers.get("Authorization"));
    }

    if (token != null && token.startsWith("Bearer ")) {
      token = token.substring(7);
    }

    try {
      SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

      // JJWT 검증 및 Claims 추출
      Claims claims = Jwts.parser()
          .verifyWith(key)
          .build()
          .parseSignedClaims(token)
          .getPayload();

      String userId = claims.getSubject();
      context.getLogger().log("Verified User: " + userId);

      // Context 생성
      Map<String, Object> contextMap = new HashMap<>();
      contextMap.put("principalId", userId);

      // HTTP API v2 Simple Response Map 구성
      Map<String, Object> response = new HashMap<>();
      response.put("isAuthorized", true);
      response.put("context", contextMap);

      return response;
    } catch (Exception e) {
      context.getLogger().log("Token verification failed: " + e.getMessage());

      Map<String, Object> response = new HashMap<>();
      response.put("isAuthorized", false);

      return response;
    }
  }
}