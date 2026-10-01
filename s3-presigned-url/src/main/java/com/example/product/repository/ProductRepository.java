package com.example.product.repository;

import com.example.product.entity.Product;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

public class ProductRepository {

  private final DynamoDbTable<Product> productTable;

  public ProductRepository(DynamoDbEnhancedClient enhancedClient) {
    String tableName = System.getenv("TABLE_NAME");
    this.productTable = enhancedClient.table(tableName, TableSchema.fromBean(Product.class));
  }

  public void save(Product product) {
    productTable.putItem(product);
  }

  public Product findById(String productId) {
    Key key = Key.builder()
      .partitionValue(productId)
      .build();
    return productTable.getItem(key);
  }
}