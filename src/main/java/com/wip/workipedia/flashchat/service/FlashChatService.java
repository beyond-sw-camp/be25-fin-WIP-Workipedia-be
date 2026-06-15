package com.wip.workipedia.flashchat.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.flashchat.domain.FlashChatPolicy;
import com.wip.workipedia.flashchat.dto.FlashChatMessageBroadcast;
import com.wip.workipedia.flashchat.dto.FlashChatMessageResponse;
import com.wip.workipedia.flashchat.dto.SendMessageRequest;
import com.wip.workipedia.flashchat.repository.FlashChatPolicyRepository;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

// FlashChat 일반 사용자 기능: 메시지 전송(쿨다운·금지어 검사 포함)과 활성 메시지 목록 조회.
// 메시지는 Redis Hash에 저장하고, ZSet(score = 만료 epoch ms)으로 TTL 관리.
@Service
@RequiredArgsConstructor
public class FlashChatService {

    private static final Long DEFAULT_POLICY_ID = 1L;
    private static final String MESSAGES_ZSET_KEY = "flash-chat:messages";
    private static final String MESSAGE_KEY_PREFIX = "flash-chat:msg:";
    private static final String COOLDOWN_KEY_PREFIX = "flash-chat:cooldown:";

    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final FlashChatPolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // 쿨다운·금지어 검사 후 Redis Hash에 저장, ZSet에 만료 시각 등록, WebSocket 브로드캐스트.
    public FlashChatMessageBroadcast sendMessage(Long userId, SendMessageRequest request) {
        FlashChatPolicy policy = loadPolicy();
        User user = getUser(userId);

        String cooldownKey = COOLDOWN_KEY_PREFIX + userId;
        if (policy.getSendCooldownSeconds() > 0) {
            // setIfAbsent = atomic SET NX EX. hasKey + set 분리 시 TOCTOU 발생 가능.
            boolean acquired = Boolean.TRUE.equals(
                    stringRedisTemplate.opsForValue()
                            .setIfAbsent(cooldownKey, "1", Duration.ofSeconds(policy.getSendCooldownSeconds())));
            if (!acquired) {
                throw new CustomException(ErrorType.FLASH_CHAT_COOLDOWN);
            }
        }

        checkBannedWords(request.content(), policy.getBannedWords());

        String messageId = UUID.randomUUID().toString();
        long nowEpoch = System.currentTimeMillis();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime expiresAt = createdAt.plusSeconds(policy.getMessageTtlSeconds());

        String messageKey = MESSAGE_KEY_PREFIX + messageId;
        Map<String, String> hash = new HashMap<>();
        hash.put("userId", userId.toString());
        hash.put("nickname", user.getNickname());
        hash.put("content", request.content());
        // Redis Hash는 null 값을 저장할 수 없어 빈 문자열로 대체.
        hash.put("replyToId", request.replyToId() != null ? request.replyToId() : "");
        hash.put("createdAt", createdAt.toString());
        hash.put("expiresAt", expiresAt.toString());

        stringRedisTemplate.opsForHash().putAll(messageKey, hash);
        stringRedisTemplate.expire(messageKey, Duration.ofSeconds(policy.getMessageTtlSeconds()));
        // ZSet score = 만료 epoch ms → rangeByScore/removeRangeByScore로 만료 메시지를 O(log n)에 정리.
        long expiresEpoch = nowEpoch + (policy.getMessageTtlSeconds() * 1000L);
        stringRedisTemplate.opsForZSet().add(MESSAGES_ZSET_KEY, messageId, (double) expiresEpoch);

        FlashChatMessageBroadcast broadcast = FlashChatMessageBroadcast.of(
                messageId, userId, user.getNickname(),
                request.content(), request.replyToId(), createdAt, expiresAt);
        messagingTemplate.convertAndSend("/topic/flash-chat", broadcast);
        return broadcast;
    }

    // 만료된 항목을 먼저 정리(lazy cleanup)한 뒤 유효한 메시지만 반환.
    public List<FlashChatMessageResponse> getActiveMessages() {
        long now = System.currentTimeMillis();
        // 조회 시점에 만료 항목을 제거(lazy cleanup). ZSet TTL은 개별 Hash의 만료를 보장하지 않음.
        stringRedisTemplate.opsForZSet().removeRangeByScore(MESSAGES_ZSET_KEY, 0, now);

        Set<String> ids = stringRedisTemplate.opsForZSet()
                .rangeByScore(MESSAGES_ZSET_KEY, now, Double.MAX_VALUE);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return ids.stream()
                .map(id -> {
                    Map<Object, Object> data =
                            stringRedisTemplate.opsForHash().entries(MESSAGE_KEY_PREFIX + id);
                    if (data.isEmpty()) {
                        return null;
                    }
                    return toMessageResponse(id, data);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private FlashChatPolicy loadPolicy() {
        return policyRepository.findById(DEFAULT_POLICY_ID)
                .orElseThrow(() -> new CustomException(ErrorType.FLASH_CHAT_POLICY_NOT_FOUND));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorType.AUTH_USER_NOT_FOUND));
    }

    private void checkBannedWords(String content, String bannedWordsJson) {
        if (bannedWordsJson == null) {
            return;
        }
        try {
            List<String> words = objectMapper.readValue(bannedWordsJson,
                    new TypeReference<List<String>>() {});
            for (String word : words) {
                if (content.contains(word)) {
                    throw new CustomException(ErrorType.FLASH_CHAT_BANNED_WORD);
                }
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            // 정책 JSON이 손상된 경우 채팅은 계속 허용.
        }
    }

    private FlashChatMessageResponse toMessageResponse(String id, Map<Object, Object> data) {
        return new FlashChatMessageResponse(
                id,
                Long.parseLong((String) data.get("userId")),
                (String) data.get("nickname"),
                (String) data.get("content"),
                "".equals(data.get("replyToId")) ? null : (String) data.get("replyToId"),
                LocalDateTime.parse((String) data.get("createdAt")),
                LocalDateTime.parse((String) data.get("expiresAt")));
    }
}
