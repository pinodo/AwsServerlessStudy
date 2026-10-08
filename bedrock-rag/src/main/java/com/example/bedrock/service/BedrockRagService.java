package com.example.bedrock.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.example.bedrock.dto.RagRequestDto;
import com.example.bedrock.dto.RagResponseDto;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseQuery;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseRetrievalConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseVectorSearchConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveResponse;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseRetrievalResult;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.InferenceConfiguration;
import software.amazon.awssdk.services.bedrockruntime.model.Message;
import software.amazon.awssdk.services.bedrockruntime.model.SystemContentBlock;

public class BedrockRagService {

  // AWS 콘솔에서 생성한 Knowledge Base ID (예: ABCDEF1234)
  private static final String KNOWLEDGE_BASE_ID = "FRGJQGLUPW";

  // 답변 생성에 사용할 모델 (Cross-Region Inference Profile ID)
  private static final String MODEL_ID = "global.anthropic.claude-sonnet-5-5";

  private static final int TOP_K = 5;

  private static final String SYSTEM_PROMPT = """
      당신은 제공된 문서(Context)만을 근거로 답변하는 어시스턴트입니다.
      Context에 답이 없으면 모른다고 답하세요. 추측하지 마세요.
      """;

  // 검색용 클라이언트 (Knowledge Base Retrieve)
  private final BedrockAgentRuntimeClient agentClient;
  // 답변 생성용 클라이언트 (Converse)
  private final BedrockRuntimeClient runtimeClient;

  public BedrockRagService() {
    this.agentClient = BedrockAgentRuntimeClient.builder()
        .credentialsProvider(ProfileCredentialsProvider.create("bedrock-dev"))
        .region(Region.US_EAST_1)
        .build();
    this.runtimeClient = BedrockRuntimeClient.builder()
        .credentialsProvider(ProfileCredentialsProvider.create("bedrock-dev"))
        .region(Region.US_EAST_1)
        .build();
  }

  public RagResponseDto askQuestion(RagRequestDto request) {

    // 1. Knowledge Base에서 관련 문서 검색
    RetrieveRequest retrieveRequest = RetrieveRequest.builder()
        .knowledgeBaseId(KNOWLEDGE_BASE_ID)
        .retrievalQuery(KnowledgeBaseQuery.builder().text(request.prompt()).build())
        .retrievalConfiguration(KnowledgeBaseRetrievalConfiguration.builder()
            .vectorSearchConfiguration(KnowledgeBaseVectorSearchConfiguration.builder()
                .numberOfResults(TOP_K)
                .build())
            .build())
        .build();

    RetrieveResponse retrieveResponse = agentClient.retrieve(retrieveRequest);

    // 2. 검색된 청크를 Context로 조립하고 출처(S3 URI) 수집
    List<String> sourceUris = new ArrayList<>();
    StringBuilder context = new StringBuilder();
    int idx = 1;

    for (KnowledgeBaseRetrievalResult result : retrieveResponse.retrievalResults()) {
      context.append("[").append(idx++).append("] ").append(result.content().text()).append("\n\n");

      if (result.location() != null && result.location().s3Location() != null) {
        String uri = result.location().s3Location().uri();
        if (!sourceUris.contains(uri)) {
          sourceUris.add(uri);
        }
      }
    }

    // 3. 검색 결과를 근거로 Converse 호출
    String userPrompt = "Context:\n" + context + "Question: " + request.prompt();

    Message message = Message.builder()
        .role(ConversationRole.USER)
        .content(ContentBlock.fromText(userPrompt))
        .build();

    ConverseRequest converseRequest = ConverseRequest.builder()
        .modelId(MODEL_ID)
        .system(SystemContentBlock.fromText(SYSTEM_PROMPT))
        .messages(message)
        .inferenceConfig(InferenceConfiguration.builder().maxTokens(2048).build())
        .build();

    ConverseResponse converseResponse = runtimeClient.converse(converseRequest);

    // 4. 텍스트 블록만 골라 이어 붙임 (첫 블록이 reasoning 등일 때 null 방지)
    String responseText = converseResponse.output().message().content().stream()
        .map(ContentBlock::text)
        .filter(Objects::nonNull)
        .collect(Collectors.joining());

    return new RagResponseDto(responseText, sourceUris);
  }
}
