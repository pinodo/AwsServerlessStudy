package com.example.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class User {

  private String userId;
  private String name;
  private String email;
  private String role; // 예: ADMIN, USER, MANAGER
  private String createdAt; // ISO-8601 문자열 (예: 2026-03-31T10:00:00Z)

  @DynamoDbPartitionKey
  public String getUserId() {
    return userId;
  }

  @DynamoDbSecondaryPartitionKey(indexNames = "EmailIndex")
  public String getEmail() {
    return email;
  }

  @DynamoDbSecondaryPartitionKey(indexNames = "RoleIndex")
  public String getRole() {
    return role;
  }

  @DynamoDbSecondarySortKey(indexNames = "RoleIndex")
  public String getCreatedAt() {
    return createdAt;
  }
}