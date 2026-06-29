package com.wip.workipedia.admin.flashchat.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.admin.flashchat.dto.FlashChatPolicyRequest;
import com.wip.workipedia.admin.repository.AdminLogRepository;
import com.wip.workipedia.flashchat.domain.FlashChatPolicy;
import com.wip.workipedia.flashchat.dto.FlashChatReexpireBroadcast;
import com.wip.workipedia.flashchat.repository.FlashChatPolicyRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class AdminFlashChatServiceReexpireTest {

	@Mock StringRedisTemplate stringRedisTemplate;
	@Mock SimpMessagingTemplate messagingTemplate;
	@Mock FlashChatPolicyRepository policyRepository;
	@Mock AdminLogRepository adminLogRepository;
	@Spy ObjectMapper objectMapper = new ObjectMapper();

	@Mock ZSetOperations<String, String> zSetOps;
	@SuppressWarnings("rawtypes")
	@Mock HashOperations hashOps;

	AdminFlashChatService service() {
		return new AdminFlashChatService(
			stringRedisTemplate, messagingTemplate, policyRepository, adminLogRepository, objectMapper);
	}

	@Test
	void TTL이_바뀌면_활성메시지를_재만료하고_REEXPIRE를_브로드캐스트한다() {
		FlashChatPolicy policy = org.mockito.Mockito.mock(FlashChatPolicy.class);
		given(policy.getMessageTtlSeconds()).willReturn(3600); // 기존 60분
		given(policyRepository.findById(1L)).willReturn(Optional.of(policy));

		given(stringRedisTemplate.opsForZSet()).willReturn(zSetOps);
		given(stringRedisTemplate.opsForHash()).willReturn(hashOps);
		given(zSetOps.range("flash-chat:messages", 0, -1)).willReturn(Set.of("m1"));
		given(stringRedisTemplate.hasKey("flash-chat:msg:m1")).willReturn(true);

		service().updatePolicy(1L, new FlashChatPolicyRequest(60, 0, null)); // 1분으로 변경

		verify(zSetOps).add(eq("flash-chat:messages"), eq("m1"), anyDouble());
		verify(stringRedisTemplate).expire(eq("flash-chat:msg:m1"), any());
		verify(messagingTemplate).convertAndSend(eq("/topic/flash-chat"), any(FlashChatReexpireBroadcast.class));
	}

	@Test
	void TTL이_그대로면_재만료하지_않는다() {
		FlashChatPolicy policy = org.mockito.Mockito.mock(FlashChatPolicy.class);
		given(policy.getMessageTtlSeconds()).willReturn(60);
		given(policyRepository.findById(1L)).willReturn(Optional.of(policy));

		service().updatePolicy(1L, new FlashChatPolicyRequest(60, 0, null)); // 동일

		verify(messagingTemplate, never()).convertAndSend(eq("/topic/flash-chat"), any(FlashChatReexpireBroadcast.class));
	}
}
