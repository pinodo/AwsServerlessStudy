package com.example.product.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Product {

  private String productId;
  private String name;
  private int price;
  private String imageKey;
  private String imageUrl; // 응답 시 사용 (DynamoDB 저장 제외 원하면 @DynamoDbIgnore 처리 가능)

  @DynamoDbPartitionKey
  public String getProductId() {
    return productId;
  }
}