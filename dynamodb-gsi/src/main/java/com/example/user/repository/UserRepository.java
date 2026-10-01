package com.example.user.repository;

import java.util.List;
import java.util.Optional;

import com.example.user.entity.User;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

public class UserRepository {

  private final DynamoDbTable<User> userTable;
  
  // 사용할 인덱스를 가져온 뒤, QueryConditional.keyEqualTo(...)를 이용해 조회함
  private final DynamoDbIndex<User> emailIndex;
  private final DynamoDbIndex<User> roleIndex;

  public UserRepository(DynamoDbEnhancedClient enhancedClient) {
    String tableName = System.getenv("TABLE_NAME");
    this.userTable = enhancedClient.table(tableName, TableSchema.fromBean(User.class));
    this.emailIndex = userTable.index("EmailIndex");
    this.roleIndex = userTable.index("RoleIndex");
  }

  public void save(User user) {
    userTable.putItem(user);
  }

  public User findById(String userId) {
    Key key = Key.builder()
      .partitionValue(userId)
      .build();
    return userTable.getItem(key);
  }

  // GSI 1: Email로 단건 조회 (Query)
  public Optional<User> findByEmail(String email) {
    QueryConditional queryConditional = QueryConditional.keyEqualTo(
      Key.builder().partitionValue(email).build()
    );

    return emailIndex.query(queryConditional)
      .stream()
      .flatMap(page -> page.items().stream())
      .findFirst();
  }

  // GSI 2: Role로 목록 조회 (Query)
  public List<User> findByRole(String role) {
    QueryConditional queryConditional = QueryConditional.keyEqualTo(
      Key.builder().partitionValue(role).build()
    );

    return roleIndex.query(queryConditional)
      .stream()
      .flatMap(page -> page.items().stream())
      .toList();
  }
}