package com.example.product.controller;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.example.common.AbstractLambdaHandler;
import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;
import com.example.product.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class ProductHandler extends AbstractLambdaHandler {

  private final ProductService productService;

  public ProductHandler() {
    DynamoDbClient ddb = DynamoDbClient.builder()
      .httpClient(UrlConnectionHttpClient.create())
      .region(Region.AP_NORTHEAST_2)
      .build();
    DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
      .dynamoDbClient(ddb)
      .build();
    ProductRepository repository = new ProductRepository(enhancedClient);
    this.productService = new ProductService(repository);
  }

  @Override
  protected APIGatewayV2HTTPResponse processRequest(APIGatewayV2HTTPEvent input, Context context) throws Exception {
    String method = input.getRequestContext().getHttp().getMethod();
    String path = input.getRequestContext().getHttp().getPath();

    if ("POST".equalsIgnoreCase(method) && "/api/products".equals(path)) {
      return createProduct(input);
    } else if ("GET".equalsIgnoreCase(method)) {
      return getProduct(input);
    }

    return response(405, Map.of("error", "Method Not Allowed"));
  }

  private APIGatewayV2HTTPResponse createProduct(APIGatewayV2HTTPEvent input) throws JsonProcessingException {
    String body = input.getBody();
    if (body == null || body.isBlank()) {
      throw new IllegalArgumentException("요청 Body가 없습니다.");
    }
    Product product = objectMapper.readValue(body, Product.class);
    Product newProduct = productService.createProduct(product);
    return response(201, newProduct);
  }

  private APIGatewayV2HTTPResponse getProduct(APIGatewayV2HTTPEvent input) {
    String productId = extractPathVariable(input, "productId");
    Product product = productService.getProduct(productId);
    return response(200, product);
  }
}