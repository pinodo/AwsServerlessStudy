package com.example.product.service;

import java.util.UUID;

import com.example.product.entity.Product;
import com.example.product.exception.ProductNotFoundException;
import com.example.product.repository.ProductRepository;

public class ProductService {

  private final ProductRepository repository;
  private final String bucketName = System.getenv("BUCKET_NAME");

  public ProductService(ProductRepository repository) {
    this.repository = repository;
  }

  public Product createProduct(Product product) {
    String name = product.getName();
    String imageKey = product.getImageKey();

    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("상품명은 필수 입력값입니다.");
    }
    
    product.setProductId(UUID.randomUUID().toString());
    repository.save(product);

    // 응답용 ImageUrl
    if (imageKey != null && !imageKey.isBlank()) {
      product.setImageUrl(String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s", bucketName, imageKey));
    }

    return product;
  }

  public Product getProduct(String productId) {
    Product product = repository.findById(productId);
    if (product == null) {
      throw new ProductNotFoundException("상품을 찾을 수 없습니다. ID: " + productId);
    }
    
    String imageKey = product.getImageKey();
    if (imageKey != null && !imageKey.isBlank()) {
      product.setImageUrl(String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s", bucketName, imageKey));
    }

    return product;
  }
}