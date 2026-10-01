package com.example.user.service;

import java.util.UUID;

import com.example.user.dto.UserCreateRequest;
import com.example.user.dto.UserResponse;
import com.example.user.dto.UserUpdateRequest;
import com.example.user.entity.User;
import com.example.user.exception.UserNotFoundException;
import com.example.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
public class UserService {

  private final UserRepository repository;

  public UserResponse createUser(UserCreateRequest request) {
    if (request.age() < 0) {
      throw new IllegalArgumentException("나이는 음수일 수 없습니다.");
    }
    String userId = UUID.randomUUID().toString();
    User user = User.builder()
      .userId(userId)
      .name(request.name())
      .age(request.age())
      .build();
    repository.save(user);
    return UserResponse.from(user);
  }

  public UserResponse getUser(String userId) {
    User foundUser = repository.findById(userId);
    if (foundUser == null) {
      throw new UserNotFoundException("User를 찾을 수 없습니다. User ID: " + userId);
    }
    return UserResponse.from(foundUser);
  }

  public UserResponse updateUser(String userId, UserUpdateRequest request) {
    if (request.age() < 0) {
      throw new IllegalArgumentException("나이는 음수일 수 없습니다.");
    }

    User existingUser = repository.findById(userId);

    existingUser.setAge(request.age());
    existingUser.setName(request.name());

    return UserResponse.from(existingUser);
  }

  public void deleteUser(String userId) {
    getUser(userId);
    repository.deleteById(userId);
  }
}