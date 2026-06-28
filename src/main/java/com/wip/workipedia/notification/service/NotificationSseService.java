package com.wip.workipedia.notification.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 실시간 알림(SSE) 구독을 관리한다. 사용자별로 열린 SseEmitter들을 들고 있다가
 * 새 알림이 생기면 해당 사용자의 모든 연결로 push 한다.
 *
 * <p>현재 BE는 단일 인스턴스라 in-memory 레지스트리로 충분하다. 다중 인스턴스로
 * 확장하면 인스턴스 간 알림 전파(예: Redis pub/sub)가 추가로 필요하다.
 */
@Service
@Slf4j
public class NotificationSseService {

    // SSE 연결 유지 시간. 프록시/브라우저가 끊으면 FE의 EventSource가 자동 재연결한다.
    private static final long TIMEOUT_MS = 60L * 60 * 1000;

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /** 사용자의 SSE 연결을 등록하고 emitter를 반환한다. 초기 connect 이벤트를 한 번 보낸다. */
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emitters.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            remove(userId, emitter);
        });
        emitter.onError(e -> remove(userId, emitter));

        // 연결 직후 한 번 보내 프록시 버퍼링/연결 확인 용도로 쓴다.
        trySend(userId, emitter, SseEmitter.event().name("connect").data("connected"));
        return emitter;
    }

    /** 해당 사용자의 모든 SSE 연결로 알림 데이터를 push 한다. 구독이 없으면 아무 일도 하지 않는다. */
    public void send(Long userId, Object payload) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) {
            return;
        }
        for (SseEmitter emitter : userEmitters) {
            trySend(userId, emitter, SseEmitter.event().name("notification").data(payload));
        }
    }

    /** 현재 사용자에게 열려 있는 SSE 연결 수. (모니터링·테스트용) */
    public int activeConnections(Long userId) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        return userEmitters == null ? 0 : userEmitters.size();
    }

    // 프록시/CDN/ALB가 idle 연결을 끊지 않도록 주기적으로 ping(주석 이벤트)을 보낸다.
    // 간격은 체인에서 가장 짧은 타임아웃(CloudFront origin 응답 기본 30초)보다 충분히 짧게 둔다.
    @Scheduled(fixedRate = 15_000)
    public void heartbeat() {
        emitters.forEach((userId, userEmitters) -> {
            for (SseEmitter emitter : userEmitters) {
                trySend(userId, emitter, SseEmitter.event().comment("ping"));
            }
        });
    }

    private void trySend(Long userId, SseEmitter emitter, SseEmitter.SseEventBuilder event) {
        try {
            emitter.send(event);
        } catch (IOException | IllegalStateException e) {
            // 끊긴 연결: 정리한다. (FE EventSource가 곧 재연결한다)
            remove(userId, emitter);
        }
    }

    private void remove(Long userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) {
            return;
        }
        userEmitters.remove(emitter);
        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
        }
    }
}
