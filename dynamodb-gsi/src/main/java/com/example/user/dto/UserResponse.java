package com.example.user.dto;

import com.example.user.entity.User;

import lombok.Builder;

@Builder
public record UserResponse(
    String userId,
    String name,
    String email,
    String role,
    String createdAt
) {
  // Entity -> DTO
  public static UserResponse from(User user) {
    return UserResponse.builder()
      .userId(user.getUserId())
      .name(user.getName())
      .email(user.getEmail())
      .role(user.getRole())
      .createdAt(user.getCreatedAt())
      .build();
  }
}