package com.example.user;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

public class UserHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  private final Map<Integer, String> users = new HashMap<>();

  public UserHandler() {
    users.put(100, "Junior Dev");
    users.put(200, "Senior Dev");
    users.put(300, "Project Manager");
  }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent input, Context context) {
    Map<String, String> pathParameters = input.getPathParameters();
    String strId = (pathParameters != null) ? pathParameters.get("id") : null;

    Map<String, String> headers = Map.of("Content-Type", "application/json");

    try {
      if (strId == null || strId.isBlank()) {
        return error(400, "Invalid Request: ID is missing", headers);
      }

      int targetId = Integer.parseInt(strId);
      String foundName = users.get(targetId);

      if (foundName != null) {
        String responseBody = String.format("{\"id\": %d, \"name\": \"%s\"}", targetId, foundName);
        return ok(responseBody, headers);
      } else {
        return error(404, "User not found", headers);
      }

    } catch (NumberFormatException e) {
      return error(400, "ID must be a number", headers);
    }
  }

  private APIGatewayV2HTTPResponse ok(String body, Map<String, String> headers) {
    return APIGatewayV2HTTPResponse.builder()
      .withStatusCode(200)
      .withHeaders(headers)
      .withBody(body)
      .build();
  }

  private APIGatewayV2HTTPResponse error(int statusCode, String message, Map<String, String> headers) {
    return APIGatewayV2HTTPResponse.builder()
      .withStatusCode(statusCode)
      .withHeaders(headers)
      .withBody(String.format("{\"error\": \"%s\"}", message))
      .build();
  }
}