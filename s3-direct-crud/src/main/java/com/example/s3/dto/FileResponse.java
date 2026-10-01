package com.example.s3.dto;

public record FileResponse(
    String key,
    long size,
    String lastModified
) {
}