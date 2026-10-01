package com.example.image.dto;

public record ImageResponse(
    String uploadUrl,
    String imageKey
) {
}