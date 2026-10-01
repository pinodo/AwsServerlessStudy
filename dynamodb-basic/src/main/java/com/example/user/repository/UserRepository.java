package com.example.user.repository;

import com.example.user.entity.User;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

public class UserRepository {

  private final DynamoDbTable<User> userTable;

  public UserRepository(DynamoDbEnhancedClient enhancedClient) {
    String tableName = System.getenv("TABLE_NAME");
    this.userTable = enhancedClient.table(tableName, TableSchema.fromBean(User.class));
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

  public User update(User user) {
    return userTable.updateItem(user);
  }

  public User deleteById(String userId) {
    Key key = Key.builder()
      .partitionValue(userId)
      .build();
    return userTable.deleteItem(key);
  }
}