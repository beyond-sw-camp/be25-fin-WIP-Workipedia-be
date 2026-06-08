package com.wip.workipedia.flashchat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.flashchat.domain.FlashChatPolicy;
import com.wip.workipedia.flashchat.dto.FlashChatMessageBroadcast;
import com.wip.workipedia.flashchat.dto.SendMessageRequest;
import com.wip.workipedia.flashchat.repository.AdminLogRepository;
import com.wip.workipedia.flashchat.repository.FlashChatPolicyRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FlashChatServiceTest {

    @Mock StringRedisTemplate stringRedisTemplate;
    @Mock SimpMessagingTemplate messagingTemplate;
    @Mock FlashChatPolicyRepository policyRepository;
    @Mock AdminLogRepository adminLogRepository;
    @Mock HashOperations<String, Object, Object> hashOps;
    @Mock ZSetOperations<String, String> zSetOps;
    @Mock ValueOperations<String, String> valueOps;
    @Mock ObjectMapper objectMapper;

    @InjectMocks FlashChatService flashChatService;

    private FlashChatPolicy policy;

    @BeforeEach
    void setUp() throws Exception {
        policy = createPolicy(600, 0, null);
        lenient().when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        lenient().when(stringRedisTemplate.opsForHash()).thenReturn(hashOps);
        lenient().when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOps);
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void sendMessage_정상_전송() {
        given(stringRedisTemplate.hasKey(anyString())).willReturn(false);

        SendMessageRequest request = new SendMessageRequest("연차 반차 차이가 뭐예요?", null);
        FlashChatMessageBroadcast result = flashChatService.sendMessage(request);

        assertThat(result.type()).isEqualTo("MESSAGE");
        assertThat(result.content()).isEqualTo("연차 반차 차이가 뭐예요?");
        assertThat(result.userId()).isEqualTo(1L);
        verify(hashOps).putAll(anyString(), anyMap());
        verify(zSetOps).add(eq("flash-chat:messages"), anyString(), anyDouble());
        verify(messagingTemplate).convertAndSend(eq("/topic/flash-chat"), any(FlashChatMessageBroadcast.class));
    }

    @Test
    void sendMessage_쿨다운_중_거부() {
        given(stringRedisTemplate.hasKey(contains("cooldown"))).willReturn(true);

        SendMessageRequest request = new SendMessageRequest("질문입니다", null);

        assertThatThrownBy(() -> flashChatService.sendMessage(request))
                .isInstanceOf(com.wip.workipedia.common.exception.CustomException.class);
    }

    @Test
    void sendMessage_금지어_포함_거부() throws Exception {
        FlashChatPolicy policyWithBan = createPolicy(600, 0, "[\"욕설\"]");
        given(policyRepository.findById(1L)).willReturn(Optional.of(policyWithBan));
        given(stringRedisTemplate.hasKey(anyString())).willReturn(false);
        given(objectMapper.readValue(eq("[\"욕설\"]"), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .willReturn(java.util.List.of("욕설"));

        SendMessageRequest request = new SendMessageRequest("여기 욕설 있음", null);

        assertThatThrownBy(() -> flashChatService.sendMessage(request))
                .isInstanceOf(com.wip.workipedia.common.exception.CustomException.class);
    }

    @Test
    void deleteMessage_존재하는_메시지_삭제() {
        String messageId = "test-uuid-1234";
        given(stringRedisTemplate.hasKey("flash-chat:msg:" + messageId)).willReturn(true);

        flashChatService.deleteMessage(messageId);

        verify(stringRedisTemplate).delete("flash-chat:msg:" + messageId);
        verify(zSetOps).remove("flash-chat:messages", messageId);
        verify(adminLogRepository).save(any(com.wip.workipedia.flashchat.domain.AdminLog.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/flash-chat"), any(com.wip.workipedia.flashchat.dto.FlashChatDeleteBroadcast.class));
    }

    @Test
    void deleteMessage_없는_메시지_예외() {
        given(stringRedisTemplate.hasKey(anyString())).willReturn(false);

        assertThatThrownBy(() -> flashChatService.deleteMessage("nonexistent"))
                .isInstanceOf(com.wip.workipedia.common.exception.CustomException.class);
    }

    private FlashChatPolicy createPolicy(int ttl, int cooldown, String bannedWords) throws Exception {
        FlashChatPolicy p = new FlashChatPolicy() {};
        setField(p, "id", 1L);
        setField(p, "messageTtlSeconds", ttl);
        setField(p, "sendCooldownSeconds", cooldown);
        setField(p, "bannedWords", bannedWords);
        return p;
    }

    private void setField(Object target, String field, Object value) throws Exception {
        var f = FlashChatPolicy.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
}
