package com.example.user.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.example.user.dto.UserCreateRequest;
import com.example.user.dto.UserResponse;
import com.example.user.entity.User;
import com.example.user.exception.UserNotFoundException;
import com.example.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserService {

  private final UserRepository repository;

  public UserResponse createUser(UserCreateRequest request) {
    String name = request.name();
    String email = request.email();
    String role = request.role();

    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("이메일은 필수 입력값입니다.");
    }
    if (role == null || role.isBlank()) {
      throw new IllegalArgumentException("Role은 필수 입력값입니다.");
    }

    // 이메일 중복 체크 (GSI 사용)
    repository.findByEmail(email).ifPresent(u -> {
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + email);
    });

    User user = User.builder()
        .userId(UUID.randomUUID().toString())
        .name(name)
        .email(email)
        .role(role)
        .createdAt(Instant.now().toString())
        .build();

    repository.save(user);
    return UserResponse.from(user);
  }

  public UserResponse getUser(String userId) {
    User foundUser = repository.findById(userId);
    if (foundUser == null) {
      throw new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId);
    }
    return UserResponse.from(foundUser);
  }

  public UserResponse getUserByEmail(String email) {
    return repository.findByEmail(email)
        .map(UserResponse::from)
        .orElseThrow(() -> new UserNotFoundException("이메일에 해당하는 사용자가 없습니다: " + email));
  }

  public List<UserResponse> getUsersByRole(String role) {
    return repository.findByRole(role)
        .stream()
        .map(UserResponse::from)
        .toList();
  }
}