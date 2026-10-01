package com.example.user.controller;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse.APIGatewayV2HTTPResponseBuilder;
import com.example.user.dto.UserCreateRequest;
import com.example.user.dto.UserResponse;
import com.example.user.dto.UserUpdateRequest;
import com.example.user.exception.UserNotFoundException;
import com.example.user.repository.UserRepository;
import com.example.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

public class UserHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  private final UserService userService;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public UserHandler() {
    DynamoDbClient ddb = DynamoDbClient.builder()
      .httpClient(UrlConnectionHttpClient.create())  // Cold Start 최적화: UrlConnectionHttpClient 명시적 사용
      .region(Region.AP_NORTHEAST_2)
      .build();
    DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
      .dynamoDbClient(ddb)
      .build();
    UserRepository repository = new UserRepository(enhancedClient);
    this.userService = new UserService(repository);
  }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent input, Context context) {
    LambdaLogger logger = context.getLogger();
    try {
      String method = input.getRequestContext().getHttp().getMethod();

      if ("POST".equalsIgnoreCase(method)) {
        return createUser(input);
      } else if ("GET".equalsIgnoreCase(method)) {
        return getUser(input);
      } else if ("PUT".equalsIgnoreCase(method)) {
        return updateUser(input);
      } else if ("DELETE".equalsIgnoreCase(method)) {
        return deleteUser(input);
      }

      return response(405, Map.of("error", "Method Not Allowed"));

    } catch (IllegalArgumentException e) {
      logger.log("Illegal Argument Exception");
      return response(400, Map.of("error", "Bad Request", "message", e.getMessage()));

    } catch (UserNotFoundException e) {
      logger.log("User Not Found Exception");
      return response(404, Map.of("error", "Not Found", "message", e.getMessage()));
      
    } catch (SdkClientException e) {
      logger.log("AWS SDK Client Exception");
      return response(503, Map.of("error", "Service Unavailable", "message", e.getMessage()));

    } catch (DynamoDbException e) {
      logger.log("DynamoDB Exception");
      return response(500, Map.of("error", "Internal Server Error", "message", e.getMessage()));

    } catch (Exception e) {
      logger.log("Unhandled Exception");
      return response(500, Map.of("error", "Internal Server Error", "message", e.getMessage()));
    }
  }

  private APIGatewayV2HTTPResponse createUser(APIGatewayV2HTTPEvent input) throws JsonProcessingException {
    String body = input.getBody();
    if (body == null || body.isBlank()) {
      throw new IllegalArgumentException("요청 Body가 없습니다.");
    }
    UserCreateRequest user = objectMapper.readValue(body, UserCreateRequest.class);
    UserResponse newUser = userService.createUser(user);
    return response(201, newUser);
  }

  private APIGatewayV2HTTPResponse getUser(APIGatewayV2HTTPEvent input) {
    String userId = extractUserId(input);
    UserResponse foundUser = userService.getUser(userId);
    return response(200, foundUser);
  }

  private APIGatewayV2HTTPResponse updateUser(APIGatewayV2HTTPEvent input) throws JsonProcessingException {
    String userId = extractUserId(input);
    String body = input.getBody();
    if (body == null || body.isBlank()) {
      throw new IllegalArgumentException("요청 Body가 없습니다.");
    }
    UserUpdateRequest updateUserRequest = objectMapper.readValue(body, UserUpdateRequest.class);
    UserResponse updatedUser = userService.updateUser(userId, updateUserRequest);
    return response(200, updatedUser);
  }

  private APIGatewayV2HTTPResponse deleteUser(APIGatewayV2HTTPEvent input) {
    String userId = extractUserId(input);
    userService.deleteUser(userId);
    return response(204, null);
  }

  private String extractUserId(APIGatewayV2HTTPEvent input) {
    Map<String, String> pathParameters = input.getPathParameters();
    String userId = (pathParameters != null) ? pathParameters.get("userId") : null;
    if (userId == null || userId.isBlank()) {
      throw new IllegalArgumentException("User ID가 없습니다.");
    }
    return userId;
  }

private APIGatewayV2HTTPResponse response(int statusCode, Object body) {
    APIGatewayV2HTTPResponseBuilder builder = APIGatewayV2HTTPResponse.builder()
      .withStatusCode(statusCode);

    if (body != null) {
      try {
        String jsonBody = (body instanceof String) ? (String) body : objectMapper.writeValueAsString(body);
        builder
          .withHeaders(Map.of("Content-Type", "application/json"))
          .withBody(jsonBody);
      } catch (Exception e) {
        builder
          .withStatusCode(500)
          .withBody("{\"error\":\"JSON Serialization Error\"}");
      }
    }

    return builder.build();
  }
}