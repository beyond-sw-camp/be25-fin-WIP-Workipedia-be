package com.wip.workipedia.admin.flashchat.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.admin.domain.AdminLog;
import com.wip.workipedia.admin.flashchat.dto.FlashChatPolicyRequest;
import com.wip.workipedia.admin.flashchat.dto.FlashChatPolicyResponse;
import com.wip.workipedia.admin.repository.AdminLogRepository;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.flashchat.domain.FlashChatPolicy;
import com.wip.workipedia.flashchat.dto.FlashChatDeleteBroadcast;
import com.wip.workipedia.flashchat.dto.FlashChatReexpireBroadcast;
import com.wip.workipedia.flashchat.repository.FlashChatPolicyRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// FlashChat 관리자 기능: 메시지 강제 삭제 및 정책(TTL·쿨다운·금지어) 조회·수정.
// 관리자 조작은 AdminLog에 감사 로그를 남기고, 삭제 시 WebSocket으로 전체 브로드캐스트.
@Service
@RequiredArgsConstructor
public class AdminFlashChatService {

    private static final Long DEFAULT_POLICY_ID = 1L;
    private static final String MESSAGES_ZSET_KEY = "flash-chat:messages";
    private static final String MESSAGE_KEY_PREFIX = "flash-chat:msg:";

    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final FlashChatPolicyRepository policyRepository;
    private final AdminLogRepository adminLogRepository;
    private final ObjectMapper objectMapper;

    // Hash와 ZSet 양쪽 모두 삭제 후 감사 로그 기록. 삭제 브로드캐스트로 클라이언트 즉시 반영.
    @Transactional
    public void deleteMessage(Long adminUserId, String messageId) {
        String messageKey = MESSAGE_KEY_PREFIX + messageId;
        // hasKey()는 null 반환 가능 → Boolean.TRUE.equals()로 null-safe 비교.
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

    // 정책은 단일 레코드(id=1)로 관리.
    public FlashChatPolicyResponse getPolicyResponse() {
        return toPolicyResponse(loadPolicy());
    }

    // 금지어 목록은 DB에 JSON 문자열로 저장. null/빈 리스트면 금지어 없음으로 처리.
    // 정책 변경과 감사 로그 저장을 하나의 트랜잭션으로 묶어 정합성 보장.
    @Transactional
    public FlashChatPolicyResponse updatePolicy(Long adminUserId, FlashChatPolicyRequest request) {
        FlashChatPolicy policy = loadPolicy();
        int previousTtlSeconds = policy.getMessageTtlSeconds();

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

        // TTL이 바뀌면 현재 활성 메시지 전체의 만료를 '지금 + 새 TTL'로 재설정(옵션 B)하고 실시간 브로드캐스트한다.
        if (request.messageTtlSeconds() != previousTtlSeconds) {
            reexpireActiveMessages(request.messageTtlSeconds());
        }

        return toPolicyResponse(policy);
    }

    // 현재 활성 메시지 전체의 만료 시각을 now + ttlSeconds로 통일한다(ZSet score·Redis 키 TTL·Hash expiresAt 갱신).
    // 모든 메시지가 동일한 만료 시각을 갖게 되므로 단일 REEXPIRE 브로드캐스트로 클라이언트에 즉시 반영한다.
    private void reexpireActiveMessages(int ttlSeconds) {
        Set<String> ids = stringRedisTemplate.opsForZSet().range(MESSAGES_ZSET_KEY, 0, -1);
        if (ids == null || ids.isEmpty()) {
            return;
        }

        LocalDateTime newExpiresAt = LocalDateTime.now().plusSeconds(ttlSeconds);
        String newExpiresAtIso = newExpiresAt.toString();
        long newExpiresEpoch = System.currentTimeMillis() + (ttlSeconds * 1000L);

        for (String id : ids) {
            String messageKey = MESSAGE_KEY_PREFIX + id;
            // 이미 만료/삭제된 Hash의 잔여 ZSet 엔트리는 정리만 한다.
            if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(messageKey))) {
                stringRedisTemplate.opsForZSet().remove(MESSAGES_ZSET_KEY, id);
                continue;
            }
            stringRedisTemplate.opsForZSet().add(MESSAGES_ZSET_KEY, id, (double) newExpiresEpoch);
            stringRedisTemplate.expire(messageKey, Duration.ofSeconds(ttlSeconds));
            stringRedisTemplate.opsForHash().put(messageKey, "expiresAt", newExpiresAtIso);
        }

        messagingTemplate.convertAndSend("/topic/flash-chat",
                FlashChatReexpireBroadcast.of(newExpiresAtIso));
    }

    private FlashChatPolicy loadPolicy() {
        return policyRepository.findById(DEFAULT_POLICY_ID)
                .orElseThrow(() -> new CustomException(ErrorType.FLASH_CHAT_POLICY_NOT_FOUND));
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
