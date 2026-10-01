package com.example.common;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse.APIGatewayV2HTTPResponseBuilder;
import com.example.s3.exception.FileNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.S3Exception;

public abstract class AbstractLambdaHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  protected static final ObjectMapper objectMapper = new ObjectMapper();

  protected abstract APIGatewayV2HTTPResponse processRequest(APIGatewayV2HTTPEvent input, Context context) throws Exception;

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent input, Context context) {
    LambdaLogger logger = context.getLogger();
    try {
      return processRequest(input, context);

    } catch (IllegalArgumentException e) {
      logger.log("Illegal Argument Exception: " + e.getMessage());
      return response(400, Map.of("error", "Bad Request", "message", e.getMessage()));

    } catch (FileNotFoundException e) {
      logger.log("File Not Found Exception: " + e.getMessage());
      return response(404, Map.of("error", "Not Found", "message", e.getMessage()));

    } catch (S3Exception e) {
      logger.log("S3 Exception: " + e.getMessage());
      return response(500, Map.of("error", "S3 Storage Error", "message", e.getMessage()));

    } catch (SdkClientException e) {
      logger.log("AWS SDK Client Exception: " + e.getMessage());
      return response(503, Map.of("error", "Service Unavailable", "message", e.getMessage()));

    } catch (Exception e) {
      logger.log("Unhandled Exception: " + e.getMessage());
      return response(500, Map.of("error", "Internal Server Error", "message", e.getMessage()));
    }
  }

  protected APIGatewayV2HTTPResponse response(int statusCode, Object body) {
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