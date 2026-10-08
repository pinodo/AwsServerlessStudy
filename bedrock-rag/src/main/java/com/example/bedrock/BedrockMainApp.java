package com.example.bedrock;

import java.util.Scanner;

import com.example.bedrock.dto.RagRequestDto;
import com.example.bedrock.dto.RagResponseDto;
import com.example.bedrock.service.BedrockRagService;

public class BedrockMainApp {
  public static void main(String[] args) {

    System.out.print("풀빌라 운영에 대해 궁금한 점을 물어보세요 > ");
    Scanner sc = new Scanner(System.in);
    String question = sc.nextLine();

    BedrockRagService ragService = new BedrockRagService();

    RagRequestDto request = new RagRequestDto(question);
    
    System.out.println("질문: " + request.prompt());
    System.out.println("답변 생성 중... (Knowledge Base 검색 및 Claude 추론)\n");

    RagResponseDto response = ragService.askQuestion(request);

    System.out.println("=== AI 답변 ===");
    System.out.println(response.responseText());
    
    System.out.println("\n=== 참고한 문서 출처 (Citations) ===");
    for (String uri : response.sourceUris()) {
      System.out.println("- " + uri);
    }

    sc.close();
  }
}