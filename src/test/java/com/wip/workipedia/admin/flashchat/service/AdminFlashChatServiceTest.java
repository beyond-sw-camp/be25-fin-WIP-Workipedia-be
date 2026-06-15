package com.wip.workipedia.admin.flashchat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.admin.domain.AdminLog;
import com.wip.workipedia.admin.repository.AdminLogRepository;
import com.wip.workipedia.flashchat.domain.FlashChatPolicy;
import com.wip.workipedia.flashchat.dto.FlashChatDeleteBroadcast;
import com.wip.workipedia.flashchat.repository.FlashChatPolicyRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminFlashChatServiceTest {

    @Mock StringRedisTemplate stringRedisTemplate;
    @Mock SimpMessagingTemplate messagingTemplate;
    @Mock FlashChatPolicyRepository policyRepository;
    @Mock AdminLogRepository adminLogRepository;
    @Mock ZSetOperations<String, String> zSetOps;
    @Mock ObjectMapper objectMapper;

    @InjectMocks AdminFlashChatService adminFlashChatService;

    @BeforeEach
    void setUp() throws Exception {
        FlashChatPolicy policy = createPolicy(600, 0, null);
        lenient().when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        lenient().when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOps);
    }

    @Test
    void deleteMessage_존재하는_메시지_삭제() {
        String messageId = "test-uuid-1234";
        given(stringRedisTemplate.hasKey("flash-chat:msg:" + messageId)).willReturn(true);

        adminFlashChatService.deleteMessage(1L, messageId);

        verify(stringRedisTemplate).delete("flash-chat:msg:" + messageId);
        verify(zSetOps).remove("flash-chat:messages", messageId);
        verify(adminLogRepository).save(any(AdminLog.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/flash-chat"), any(FlashChatDeleteBroadcast.class));
    }

    @Test
    void deleteMessage_없는_메시지_예외() {
        given(stringRedisTemplate.hasKey(anyString())).willReturn(false);

        assertThatThrownBy(() -> adminFlashChatService.deleteMessage(1L, "nonexistent"))
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
