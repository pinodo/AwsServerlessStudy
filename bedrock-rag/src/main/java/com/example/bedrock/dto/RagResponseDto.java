package com.example.bedrock.dto;

import java.util.List;

public record RagResponseDto(
  String responseText,
  List<String> sourceUris  // 출처(Citation) 리스트
) {
}