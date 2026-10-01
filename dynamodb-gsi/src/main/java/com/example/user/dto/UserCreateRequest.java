package com.example.user.dto;

public record UserCreateRequest(
    String name,
    String email,
    String role
) {
}