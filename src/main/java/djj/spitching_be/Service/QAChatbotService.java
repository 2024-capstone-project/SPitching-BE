package djj.spitching_be.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Domain.PresentationSlide;
import djj.spitching_be.Domain.QASession;
import djj.spitching_be.Dto.ChatMessageDto;
import djj.spitching_be.Dto.QASessionDto;
import djj.spitching_be.Repository.PresentationRepository;
import djj.spitching_be.Repository.PresentationSlideRepository;
import djj.spitching_be.Repository.QASessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QAChatbotService {

    private final PresentationRepository presentationRepository;
    private final PresentationSlideRepository presentationSlideRepository;
    private final QASessionRepository qaSessionRepository;
    private final ObjectMapper objectMapper;

    private final WebClient webClient;

    @Value("${openai.secret-key}")
    private String openaiApiKey;

    @Value("${openai.model}")
    private String openaiModel;

    @Transactional
    public QASessionDto startQASession(Long presentationId) {
        // 프레젠테이션 및 슬라이드 데이터 조회
        Presentation presentation = presentationRepository.findById(presentationId)
                .orElseThrow(() -> new RuntimeException("프레젠테이션을 찾을 수 없습니다."));

        List<PresentationSlide> slides = presentationSlideRepository.findByPresentationId(presentationId); // 발표 연습을 가져옴

        if (slides.isEmpty()) {
            throw new RuntimeException("슬라이드가 없는 프레젠테이션입니다.");
        }

        // 슬라이드 대본 정보만 추출 (리스트로 저장)
        List<Map<String, Object>> scriptsList = new ArrayList<>();
        for (PresentationSlide slide : slides) {
            Map<String, Object> slideInfo = new HashMap<>();
            slideInfo.put("slideNumber", slide.getSlideNumber());
            slideInfo.put("script", slide.getScript());
            scriptsList.add(slideInfo);
        }

        // 세션 데이터 구성 (간소화)
        QASessionDto sessionDto = QASessionDto.builder()
                .presentationId(presentationId)
                .presentationTitle(presentation.getTitle())
                .scripts(scriptsList)
                .messages(new ArrayList<>())
                .build();

        // 세션 데이터를 JSON으로 직렬화
        String sessionDataJson;
        try {
            sessionDataJson = objectMapper.writeValueAsString(sessionDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("세션 데이터 직렬화에 실패했습니다.", e);
        }

        // 세션 데이터 저장
        QASession qaSession = QASession.builder()
                .presentationId(presentationId)
                .sessionData(sessionDataJson)
                .createdAt(LocalDateTime.now())
                .build();

        qaSessionRepository.save(qaSession);

        return sessionDto;
    }

    @Transactional(readOnly = true)
    public ChatMessageDto generateQuestion(Long presentationId, String userMessage) {
        // 세션 데이터 조회
        QASession qaSession = qaSessionRepository.findTopByPresentationIdOrderByCreatedAtDesc(presentationId)
                .orElseThrow(() -> new RuntimeException("Q&A 세션을 먼저 시작해주세요."));

        QASessionDto sessionDto;
        try {
            sessionDto = objectMapper.readValue(qaSession.getSessionData(), QASessionDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("세션 데이터 역직렬화에 실패했습니다.", e);
        }

        // 대본 텍스트 추출
        StringBuilder allScripts = new StringBuilder();
        for (Map<String, Object> slide : sessionDto.getScripts()) {
            Integer slideNumber = (Integer) slide.get("slideNumber");
            String script = (String) slide.get("script");

            if (script != null && !script.isEmpty()) {
                allScripts.append("슬라이드 ").append(slideNumber)
                        .append(": ").append(script).append("\n\n");
            }
        }

        // "질문 있으신가요?"라는 메시지인지 확인
        if (userMessage.contains("질문 있으신가요?") || userMessage.contains("질문")) {
            // OpenAI API 호출하여 질문 생성
            String question = generateQuestionWithOpenAI(allScripts.toString(), sessionDto.getPresentationTitle());

            return ChatMessageDto.builder()
                    .role("assistant")
                    .content(question)
                    .timestamp(LocalDateTime.now())
                    .build();
        } else {
            // 일반 메시지 처리
            return processChatMessage(presentationId, ChatMessageDto.builder()
                    .role("user")
                    .content(userMessage)
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Transactional
    public ChatMessageDto processChatMessage(Long presentationId, ChatMessageDto userMessage) {
        // 세션 데이터 조회
        QASession qaSession = qaSessionRepository.findTopByPresentationIdOrderByCreatedAtDesc(presentationId)
                .orElseThrow(() -> new RuntimeException("Q&A 세션을 먼저 시작해주세요."));

        QASessionDto sessionDto;
        try {
            sessionDto = objectMapper.readValue(qaSession.getSessionData(), QASessionDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("세션 데이터 역직렬화에 실패했습니다.", e);
        }

        // 사용자 메시지 저장
        sessionDto.getMessages().add(userMessage);

        // 대본과 이전 메시지를 기반으로 응답 생성
        StringBuilder context = new StringBuilder();

        // 대본 정보 추가
        for (Map<String, Object> slide : sessionDto.getScripts()) {
            Integer slideNumber = (Integer) slide.get("slideNumber");
            String script = (String) slide.get("script");

            if (script != null && !script.isEmpty()) {
                context.append("슬라이드 ").append(slideNumber)
                        .append(": ").append(script).append("\n\n");
            }
        }

        // 이전 메시지 이력 추가 (최대 5개)
        int messageCount = sessionDto.getMessages().size();
        int startIndex = Math.max(0, messageCount - 5);

        List<Map<String, String>> messages = new ArrayList<>();

        // 시스템 메시지 추가
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "당신은 발표를 듣고 있는 청중입니다. 발표 내용에 대해 질문하거나 답변에 반응하세요. " +
                "\"발표 내용을 바탕으로 자연스럽고 관련성 높은 질문을 생성하세요.\" + " +
                "\"정중한 태도로 질문하되, 핵심을 관통하는 질문을 해주세요\"\n" +
                "\"또한 압박감 있는 질문을 하는 심사위원처럼 질문해주세요.\"\n"+
                "                + \"그리고 존댓말로 해주고, 사용자가 답변하면, 그 답변에 대한 꼬리질문이나 혹은 발표 내용에서 좀 더 심화된 혹은 확장된 질문도 간간히 해주세요.\"발표 내용: " + context.toString());
        messages.add(systemMessage);

        // 이전 대화 내역 추가
        for (int i = startIndex; i < messageCount; i++) {
            ChatMessageDto msg = sessionDto.getMessages().get(i);
            Map<String, String> messageMap = new HashMap<>();
            messageMap.put("role", msg.getRole());
            messageMap.put("content", msg.getContent());
            messages.add(messageMap);
        }

        // OpenAI API 호출
        String responseContent = callOpenAIApi(messages);

        // 봇 응답 생성
        ChatMessageDto botResponse = ChatMessageDto.builder()
                .role("assistant")
                .content(responseContent)
                .timestamp(LocalDateTime.now())
                .build();

        // 응답 메시지 저장
        sessionDto.getMessages().add(botResponse);

        // 세션 데이터 업데이트
        try {
            qaSession.setSessionData(objectMapper.writeValueAsString(sessionDto));
            qaSessionRepository.save(qaSession);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("세션 데이터 직렬화에 실패했습니다.", e);
        }

        return botResponse;
    }

    // 질문 있으신가요? 라고 화두를 던지는 용도
    private String generateQuestionWithOpenAI(String scriptContent, String presentationTitle) {
        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "당신은 발표를 듣고 있는 청중입니다. 발표자가 '질문 있으신가요?'라고 물었을 때, "
                + "발표 내용을 바탕으로 자연스럽고 관련성 높은 질문을 생성하세요." + "정중한 태도로 질문하되, 핵심을 관통하는 질문을 해주세요"
                + "그리고 존댓말로 해주고, 발표 내용에서 좀 더 심화된 혹은 확장된 질문도 간간히 해주세요."
                + "발표 주제: " + presentationTitle + "\n\n발표 내용: " + scriptContent);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", "발표 잘 들었습니다. 질문 하나 드리고 싶습니다.");

        messages.add(systemMessage);
        messages.add(userMessage);

        return callOpenAIApi(messages);
    }

    private String callOpenAIApi(List<Map<String, String>> messages) {
        // OpenAI API 요청 구성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", openaiModel);  // "gpt-4" 또는 "gpt-3.5-turbo"
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        try {
            // WebClient를 사용하여 OpenAI API 호출
            Map<String, Object> response = webClient.post()
                    .uri("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // 응답에서 텍스트 추출
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    return message.get("content");
                }
            }

            return "질문 생성에 실패했습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            return "OpenAI API 호출 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
}