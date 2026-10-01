package com.example.user.dto;

import com.example.user.entity.User;

import lombok.Builder;

@Builder 
public record UserResponse(
  String userId,
  String name,
  int age
) {
  public static UserResponse from(User user) {
    return UserResponse.builder()
      .userId(user.getUserId())
      .name(user.getName())
      .age(user.getAge())
      .build();
  }
}
