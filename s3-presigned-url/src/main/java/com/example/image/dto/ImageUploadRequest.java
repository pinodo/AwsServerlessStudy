package com.example.image.dto;

public record ImageUploadRequest(
    String filename,
    String contentType
) {
}