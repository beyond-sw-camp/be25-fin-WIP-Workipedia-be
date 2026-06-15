package com.wip.workipedia.chatbot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.chatbot.ai.ChatbotAiClient;
import com.wip.workipedia.chatbot.ai.ChatbotAiResponse;
import com.wip.workipedia.chatbot.domain.ChatbotMessage;
import com.wip.workipedia.chatbot.domain.ChatbotSession;
import com.wip.workipedia.chatbot.domain.NextAction;
import com.wip.workipedia.chatbot.dto.ChatbotMessageResponse;
import com.wip.workipedia.chatbot.dto.ChatbotSessionResponse;
import com.wip.workipedia.chatbot.repository.ChatbotMessageRepository;
import com.wip.workipedia.chatbot.repository.ChatbotSessionRepository;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ChatbotServiceTest {

    private final ChatbotSessionRepository sessionRepository = mock(ChatbotSessionRepository.class);
    private final ChatbotMessageRepository messageRepository = mock(ChatbotMessageRepository.class);
    private final ChatbotAiClient aiClient = mock(ChatbotAiClient.class);
    private final ChatbotService service = new ChatbotService(
            sessionRepository, messageRepository, aiClient, new ObjectMapper());

    @Test
    void createSession_저장하고_세션을_반환한다() {
        ChatbotSession session = ChatbotSession.create(1L, "테스트 세션");
        ReflectionTestUtils.setField(session, "sessionId", 10L);
        when(sessionRepository.save(any())).thenReturn(session);

        ChatbotSessionResponse response = service.createSession(1L, "테스트 세션");

        assertThat(response.sessionId()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("테스트 세션");
    }

    @Test
    void sendMessage_본인_세션이_아니면_FORBIDDEN() {
        ChatbotSession session = ChatbotSession.create(99L, "남의 세션");
        ReflectionTestUtils.setField(session, "sessionId", 5L);
        when(sessionRepository.findBySessionIdAndIsDeleted(5L, "N")).thenReturn(Optional.of(session));
        when(messageRepository.findTop10BySessionIdAndIsDeletedOrderByCreatedAtDesc(5L, "N"))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.prepareContextAndSaveUserMessage(1L, 5L, "질문"))
                .isInstanceOf(CustomException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.CHATBOT_SESSION_FORBIDDEN);
    }

    @Test
    void sendMessage_세션을_찾을_수_없으면_NOT_FOUND() {
        when(sessionRepository.findBySessionIdAndIsDeleted(999L, "N")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.prepareContextAndSaveUserMessage(1L, 999L, "질문"))
                .isInstanceOf(CustomException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.CHATBOT_SESSION_NOT_FOUND);
    }

    @Test
    void prepareContext_SYSTEM_메시지는_context에서_제외된다() {
        ChatbotSession session = ChatbotSession.create(1L, "내 세션");
        ReflectionTestUtils.setField(session, "sessionId", 5L);
        when(sessionRepository.findBySessionIdAndIsDeleted(5L, "N")).thenReturn(Optional.of(session));

        ChatbotMessage systemMsg = ChatbotMessage.ofSystem(5L, "시스템 메시지");
        ChatbotMessage userMsg = ChatbotMessage.ofUser(5L, "이전 질문");
        ReflectionTestUtils.setField(systemMsg, "messageId", 1L);
        ReflectionTestUtils.setField(userMsg, "messageId", 2L);
        when(messageRepository.findTop10BySessionIdAndIsDeletedOrderByCreatedAtDesc(5L, "N"))
                .thenReturn(List.of(userMsg, systemMsg));
        when(messageRepository.save(any())).thenReturn(userMsg);

        service.prepareContextAndSaveUserMessage(1L, 5L, "새 질문");

        verify(messageRepository).save(any()); // USER 메시지 저장됨
    }

    @Test
    void saveAssistantMessage_CREATE_TICKET_action이면_answerable_false() {
        ChatbotAiResponse aiResponse = new ChatbotAiResponse("티켓을 등록해드릴게요.", List.of(), null, "CREATE_TICKET");
        ChatbotMessage assistantMsg = ChatbotMessage.ofAssistant(5L, "티켓을 등록해드릴게요.", false, NextAction.CREATE_TICKET, null);
        ReflectionTestUtils.setField(assistantMsg, "messageId", 10L);
        when(messageRepository.save(any())).thenReturn(assistantMsg);

        ChatbotMessageResponse response = service.saveAssistantMessage(5L, aiResponse);

        assertThat(response.answerable()).isFalse();
        assertThat(response.nextAction()).isEqualTo(NextAction.CREATE_TICKET);
    }

    @Test
    void saveAssistantMessage_일반_답변이면_answerable_true() {
        ChatbotAiResponse aiResponse = new ChatbotAiResponse("휴가는 HR 시스템에서 신청합니다.", List.of(), null, "SHOW_SOURCES");
        ChatbotMessage assistantMsg = ChatbotMessage.ofAssistant(5L, "휴가는 HR 시스템에서 신청합니다.", true, NextAction.SHOW_SOURCES, null);
        ReflectionTestUtils.setField(assistantMsg, "messageId", 11L);
        when(messageRepository.save(any())).thenReturn(assistantMsg);

        ChatbotMessageResponse response = service.saveAssistantMessage(5L, aiResponse);

        assertThat(response.answerable()).isTrue();
    }
}
