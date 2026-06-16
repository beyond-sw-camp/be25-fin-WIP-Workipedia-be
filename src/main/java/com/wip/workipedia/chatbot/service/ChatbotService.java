package com.wip.workipedia.chatbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.admin.aiprompt.service.AdminAiPromptService;
import com.wip.workipedia.chatbot.ai.ChatbotAiClient;
import com.wip.workipedia.chatbot.ai.ChatbotAiRequest;
import com.wip.workipedia.chatbot.ai.ChatbotAiResponse;
import com.wip.workipedia.chatbot.ai.SessionMessage;
import com.wip.workipedia.chatbot.domain.ChatbotMessage;
import com.wip.workipedia.chatbot.domain.ChatbotSession;
import com.wip.workipedia.chatbot.domain.NextAction;
import com.wip.workipedia.chatbot.domain.SenderType;
import com.wip.workipedia.chatbot.dto.ChatbotMessageRequest;
import com.wip.workipedia.chatbot.dto.ChatbotMessageResponse;
import com.wip.workipedia.chatbot.dto.ChatbotSessionResponse;
import com.wip.workipedia.chatbot.repository.ChatbotMessageRepository;
import com.wip.workipedia.chatbot.repository.ChatbotSessionRepository;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotService {

    // AI가 답을 찾지 못해 빈 답변을 반환했을 때 사용자에게 보여줄 안내 메시지
    private static final String NO_ANSWER_MESSAGE =
            "워키 게시판·매뉴얼에서 답을 찾지 못했습니다. 질문을 등록하시겠습니까?";

    private final ChatbotSessionRepository sessionRepository;
    private final ChatbotMessageRepository messageRepository;
    private final ChatbotAiClient chatbotAiClient;
    private final AdminAiPromptService adminAiPromptService;
    private final ObjectMapper objectMapper;

    // self-injection: @Transactional 서브메서드를 같은 빈 내에서 프록시로 호출하기 위해 필요
    // (직접 this.method()로 호출하면 트랜잭션이 적용되지 않음)
    @Lazy
    @Autowired
    private ChatbotService self;

    // 새 챗봇 세션 생성 후 반환
    @Transactional
    public ChatbotSessionResponse createSession(Long userId, String title) {
        ChatbotSession session = ChatbotSession.create(userId, title);
        return ChatbotSessionResponse.from(sessionRepository.save(session));
    }

    // 본인의 세션 목록 최신순 페이징 조회
    @Transactional(readOnly = true)
    public PageResponse<ChatbotSessionResponse> getMySessions(Long userId, Pageable pageable) {
        return PageResponse.from(
                sessionRepository.findByUserIdAndIsDeletedOrderByCreatedAtDesc(userId, "N", pageable)
                        .map(ChatbotSessionResponse::from)
        );
    }

    // 세션의 메시지 목록 오래된 순 페이징 조회
    @Transactional(readOnly = true)
    public PageResponse<ChatbotMessageResponse> getMessages(Long userId, Long sessionId, Pageable pageable) {
        ChatbotSession session = getSession(sessionId);
        validateOwner(userId, session);
        return PageResponse.from(
                messageRepository.findBySessionIdAndIsDeletedOrderByCreatedAtAscMessageIdAsc(sessionId, "N", pageable)
                        .map(ChatbotMessageResponse::from)
        );
    }

    // 질문 전송 진입점 — @Transactional 없음
    // 트랜잭션 경계를 분리해 외부 AI 호출이 DB 커넥션을 잡고 있지 않도록 함:
    //   1) prepareContextAndSaveUserMessage: 컨텍스트 수집 + USER 메시지 저장 (트랜잭션)
    //   2) chatbotAiClient.ask: AI 호출 (트랜잭션 없음 — 네트워크 IO)
    //   3) saveAssistantMessage: ASSISTANT 응답 저장 (트랜잭션)
    public ChatbotMessageResponse sendMessage(Long userId, Long sessionId, ChatbotMessageRequest request) {
        // 1단계: 기존 대화 컨텍스트 빌드 후 USER 메시지 저장 (트랜잭션 커밋)
        List<SessionMessage> context = self.prepareContextAndSaveUserMessage(userId, sessionId, request.question());

        // 2단계: AI 서버 호출 (실패 시 FallbackChatbotAiClient 가 안내 메시지 반환)
        String customPrompt = adminAiPromptService.findActiveCustomPrompt();
        ChatbotAiResponse aiResponse = chatbotAiClient.ask(
                new ChatbotAiRequest(request.question(), customPrompt, context)
        );

        // 3단계: ASSISTANT 응답 저장 (트랜잭션 커밋)
        return self.saveAssistantMessage(sessionId, aiResponse);
    }

    // 1단계 트랜잭션: 컨텍스트 빌드 → USER 메시지 저장
    // buildContext를 먼저 호출해 현재 질문이 컨텍스트에 중복되지 않도록 함
    @Transactional
    public List<SessionMessage> prepareContextAndSaveUserMessage(Long userId, Long sessionId, String question) {
        ChatbotSession session = getSession(sessionId);
        validateOwner(userId, session);
        // 현재 질문 저장 전에 컨텍스트를 먼저 수집해야 중복 방지
        List<SessionMessage> context = buildContext(sessionId);
        messageRepository.save(ChatbotMessage.ofUser(sessionId, question));
        return context;
    }

    // 3단계 트랜잭션: AI 응답을 ASSISTANT 메시지로 저장
    // answerable: CREATE_TICKET 액션이면 false (챗봇이 더 이상 답변하지 않고 티켓 등록 유도)
    // AI가 빈 답변을 반환하면(매뉴얼·게시판에서 답을 찾지 못한 경우) 빈 메시지를 저장하지 않고
    // 워키 질문 등록 유도 메시지로 대체한다. (빈 content 저장 시 다음 턴 컨텍스트로 전달되어 AI 422 유발)
    @Transactional
    public ChatbotMessageResponse saveAssistantMessage(Long sessionId, ChatbotAiResponse aiResponse) {
        String answer = aiResponse.answer();
        boolean hasAnswer = answer != null && !answer.isBlank();

        String content = hasAnswer ? answer : NO_ANSWER_MESSAGE;
        NextAction nextAction = hasAnswer ? parseNextAction(aiResponse.action()) : NextAction.CREATE_WORKI;
        boolean answerable = hasAnswer && nextAction != NextAction.CREATE_TICKET;
        String referencesJson = toJson(aiResponse.sources());

        ChatbotMessage assistant = messageRepository.save(
                ChatbotMessage.ofAssistant(sessionId, content, answerable, nextAction, referencesJson)
        );
        return ChatbotMessageResponse.from(assistant);
    }

    // 삭제된 세션은 조회 불가 (isDeleted = 'N' 조건)
    private ChatbotSession getSession(Long sessionId) {
        return sessionRepository.findBySessionIdAndIsDeleted(sessionId, "N")
                .orElseThrow(() -> new CustomException(ErrorType.CHATBOT_SESSION_NOT_FOUND));
    }

    private void validateOwner(Long userId, ChatbotSession session) {
        if (!session.getUserId().equals(userId)) {
            throw new CustomException(ErrorType.CHATBOT_SESSION_FORBIDDEN);
        }
    }

    // 최근 메시지 최대 10개를 AI 컨텍스트로 변환
    // DB는 최신순으로 조회하므로 reverse 후 오래된 순으로 전달
    // SYSTEM 메시지 및 빈/공백 content 메시지는 AI API가 422를 반환하므로 제외
    private List<SessionMessage> buildContext(Long sessionId) {
        List<ChatbotMessage> recent = new ArrayList<>(messageRepository
                .findTop10BySessionIdAndIsDeletedOrderByCreatedAtDescMessageIdDesc(sessionId, "N"));
        Collections.reverse(recent); // 오래된 순으로 정렬
        return recent.stream()
                .filter(m -> m.getSenderType() != SenderType.SYSTEM)
                .filter(m -> m.getContent() != null && !m.getContent().isBlank())
                .map(m -> new SessionMessage(m.getMessageId(), m.getSenderType().name(), m.getContent()))
                .toList();
    }

    private NextAction parseNextAction(String action) {
        if (action == null) return null;
        try {
            return NextAction.valueOf(action);
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 next_action 값: {}", action);
            return null;
        }
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("references JSON 직렬화 실패: {}", e.getMessage());
            return null;
        }
    }
}
