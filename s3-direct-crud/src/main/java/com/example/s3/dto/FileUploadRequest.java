package com.example.s3.dto;

public record FileUploadRequest(
    String key,
    String content
) {
}