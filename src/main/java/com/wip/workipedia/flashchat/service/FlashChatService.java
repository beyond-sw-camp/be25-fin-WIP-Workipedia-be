package com.wip.workipedia.flashchat.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.flashchat.domain.AdminLog;
import com.wip.workipedia.flashchat.domain.FlashChatPolicy;
import com.wip.workipedia.flashchat.dto.FlashChatDeleteBroadcast;
import com.wip.workipedia.flashchat.dto.FlashChatMessageBroadcast;
import com.wip.workipedia.flashchat.dto.FlashChatMessageResponse;
import com.wip.workipedia.flashchat.dto.FlashChatPolicyRequest;
import com.wip.workipedia.flashchat.dto.FlashChatPolicyResponse;
import com.wip.workipedia.flashchat.dto.SendMessageRequest;
import com.wip.workipedia.flashchat.repository.AdminLogRepository;
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
import org.springframework.transaction.annotation.Transactional;

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
    private final AdminLogRepository adminLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public FlashChatMessageBroadcast sendMessage(Long userId, SendMessageRequest request) {
        FlashChatPolicy policy = loadPolicy();
        User user = getUser(userId);

        String cooldownKey = COOLDOWN_KEY_PREFIX + userId;
        if (policy.getSendCooldownSeconds() > 0) {
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
        hash.put("replyToId", request.replyToId() != null ? request.replyToId() : "");
        hash.put("createdAt", createdAt.toString());
        hash.put("expiresAt", expiresAt.toString());

        stringRedisTemplate.opsForHash().putAll(messageKey, hash);
        stringRedisTemplate.expire(messageKey, Duration.ofSeconds(policy.getMessageTtlSeconds()));
        long expiresEpoch = nowEpoch + (policy.getMessageTtlSeconds() * 1000L);
        stringRedisTemplate.opsForZSet().add(MESSAGES_ZSET_KEY, messageId, (double) expiresEpoch);

        FlashChatMessageBroadcast broadcast = FlashChatMessageBroadcast.of(
                messageId, userId, user.getNickname(),
                request.content(), request.replyToId(), createdAt, expiresAt);
        messagingTemplate.convertAndSend("/topic/flash-chat", broadcast);
        return broadcast;
    }

    public List<FlashChatMessageResponse> getActiveMessages() {
        long now = System.currentTimeMillis();
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

    @Transactional
    public void deleteMessage(Long adminUserId, String messageId) {
        String messageKey = MESSAGE_KEY_PREFIX + messageId;
        if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(messageKey))) {
            throw new CustomException(ErrorType.FLASH_CHAT_MESSAGE_NOT_FOUND);
        }

        stringRedisTemplate.delete(messageKey);
        stringRedisTemplate.opsForZSet().remove(MESSAGES_ZSET_KEY, messageId);

        adminLogRepository.save(AdminLog.of(
                adminUserId,
                "FLASH_CHAT_MESSAGE_DELETE",
                "FLASH_CHAT_MESSAGE",
                "Flash Chat message deleted",
                "{\"messageId\":\"" + messageId + "\"}"));

        messagingTemplate.convertAndSend("/topic/flash-chat", FlashChatDeleteBroadcast.of(messageId));
    }

    public FlashChatPolicyResponse getPolicyResponse() {
        return toPolicyResponse(loadPolicy());
    }

    @Transactional
    public FlashChatPolicyResponse updatePolicy(Long adminUserId, FlashChatPolicyRequest request) {
        FlashChatPolicy policy = loadPolicy();

        String bannedWordsJson = null;
        if (request.bannedWords() != null && !request.bannedWords().isEmpty()) {
            try {
                bannedWordsJson = objectMapper.writeValueAsString(request.bannedWords());
            } catch (Exception e) {
                throw new CustomException(ErrorType.BAD_REQUEST);
            }
        }

        policy.update(request.messageTtlSeconds(), request.sendCooldownSeconds(), bannedWordsJson);

        try {
            adminLogRepository.save(AdminLog.of(
                    adminUserId,
                    "FLASH_CHAT_CONFIG_UPDATE",
                    "FLASH_CHAT_POLICY",
                    "Flash Chat policy updated",
                    objectMapper.writeValueAsString(request)));
        } catch (Exception e) {
            throw new CustomException(ErrorType.INTERNAL_ERROR);
        }

        return toPolicyResponse(policy);
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
            // Ignore malformed policy JSON and keep chat available.
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

    private FlashChatPolicyResponse toPolicyResponse(FlashChatPolicy policy) {
        List<String> bannedWords = List.of();
        if (policy.getBannedWords() != null) {
            try {
                bannedWords = objectMapper.readValue(policy.getBannedWords(),
                        new TypeReference<List<String>>() {});
            } catch (Exception ignored) {
            }
        }
        return new FlashChatPolicyResponse(
                policy.getMessageTtlSeconds(),
                policy.getSendCooldownSeconds(),
                bannedWords);
    }
}
