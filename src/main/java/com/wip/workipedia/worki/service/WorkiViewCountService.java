package com.wip.workipedia.worki.service;

import com.wip.workipedia.worki.repository.WorkiQuestionRepository;
import java.time.Duration;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 워키 질문 조회수 처리.
 *
 * <p>예전에는 상세 조회마다 곧바로 {@code UPDATE ... view_count + 1} 을 날렸다. 이 방식은
 * ① 같은 사람이 새로고침만 해도 조회수가 오르는 어뷰징을 막지 못하고,
 * ② 인기글일수록 같은 row에 UPDATE가 몰려 락 경합이 심해진다는 문제가 있었다.
 *
 * <p>그래서 조회수 집계를 Redis로 옮겼다.
 * <ul>
 *   <li><b>중복 차단</b>: {@code worki:question:viewed:{questionId}:{userId}} 키를
 *       {@code SET NX EX 600} 으로 심어, 같은 사용자의 10분 내 재조회는 무시한다.</li>
 *   <li><b>임시 누적</b>: 신규 조회만 Redis 카운터({@code ...view-pending:{questionId}})에 INCR로 쌓는다.
 *       DB는 건드리지 않으므로 요청 경로에서 DB write가 사라진다.</li>
 *   <li><b>일괄 반영</b>: 스케줄러({@link WorkiViewCountScheduler})가 주기적으로 누적분만 모아
 *       한 번의 UPDATE로 DB에 더한다.</li>
 * </ul>
 *
 * <p>트레이드오프: 조회수가 DB에 즉시 반영되지 않아(다음 flush 주기까지 지연) 약간의 실시간성을
 * 포기하는 대신, 어뷰징을 막고 DB write 부하를 크게 줄인다. 화면에는 DB값 + 미반영 누적분을
 * 합쳐 보여줘 사용자 체감 지연은 없앤다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkiViewCountService {

    /** 중복 조회 차단 키. viewed:{questionId}:{userId} 형태. 이 TTL 동안 같은 사용자의 조회는 1회로 본다. */
    private static final String DEDUP_KEY_PREFIX = "worki:question:viewed:";
    /** 아직 DB에 반영되지 않은 조회 증가분을 질문별로 모아두는 카운터 키. */
    private static final String PENDING_KEY_PREFIX = "worki:question:view-pending:";
    /** 반영할 증가분이 있는 질문 id 목록. flush 때 이 집합만 훑으면 된다. */
    private static final String DIRTY_SET_KEY = "worki:question:view-dirty";
    /** 같은 사용자의 중복 조회로 보는 시간 창(10분). */
    private static final Duration DEDUP_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;
    private final WorkiQuestionRepository questionRepository;

    /**
     * 조회 1건을 집계한다. 10분 내 같은 사용자의 재조회는 무시하고, 신규 조회만 임시 카운터에 누적한다.
     *
     * @param viewerUserId 조회한 사용자 id. 식별 불가(비로그인 등)면 어뷰징을 막을 수 없어 집계하지 않는다.
     */
    public void countView(Long questionId, Long viewerUserId) {
        if (viewerUserId == null) {
            return;
        }
        String dedupKey = DEDUP_KEY_PREFIX + questionId + ":" + viewerUserId;
        // SET NX: 키가 없을 때만 심고 true를 받는다. false면 10분 내 이미 본 것 → 중복.
        Boolean firstViewInWindow = redisTemplate.opsForValue().setIfAbsent(dedupKey, "1", DEDUP_TTL);
        if (!Boolean.TRUE.equals(firstViewInWindow)) {
            return;
        }
        redisTemplate.opsForValue().increment(PENDING_KEY_PREFIX + questionId);
        redisTemplate.opsForSet().add(DIRTY_SET_KEY, questionId.toString());
    }

    /** 화면 표시용. 아직 DB에 반영되지 않은 조회 증가분을 읽어온다(없으면 0). */
    public long getPendingCount(Long questionId) {
        String pending = redisTemplate.opsForValue().get(PENDING_KEY_PREFIX + questionId);
        return pending == null ? 0L : Long.parseLong(pending);
    }

    /**
     * Redis에 쌓인 조회 증가분을 DB에 일괄 반영한다. 스케줄러가 주기적으로 호출.
     *
     * <p><b>반영 순서가 핵심이다.</b> 카운터를 먼저 지우고(GETDEL) DB를 나중에 건드리면, DB 반영이
     * 실패했을 때 증가분이 Redis에서도 이미 사라져 영영 유실된다. 그래서 여기서는
     * <ol>
     *   <li>카운터를 <b>읽기만</b> 하고(GET, 삭제하지 않음),</li>
     *   <li>DB에 먼저 반영한 뒤,</li>
     *   <li>반영에 성공한 만큼만 Redis에서 차감(DECRBY)한다.</li>
     * </ol>
     * DB 반영이 실패하면 Redis 카운터를 손대지 않으므로 증가분이 그대로 남아 다음 주기에 재시도된다.
     *
     * <p>GETDEL이 아니라 GET + DECRBY(delta)를 쓰는 또 다른 이유: flush 도중 들어온 새 조회분은
     * 카운터에 INCR로 더해지는데, 우리가 방금 DB에 넣은 양만 정확히 빼면 그 새 조회분은 보존된다.
     *
     * <p>질문별 DB 반영은 {@link WorkiQuestionRepository#increaseViewCount}가 각자 독립 트랜잭션으로
     * 커밋하므로, 한 질문에서 예외가 나도 이미 반영된 다른 질문이 함께 롤백되지 않는다. 실패한 질문은
     * 건너뛰고 다음 주기에 다시 시도된다.
     */
    public void flushToDatabase() {
        Set<String> dirtyIds = redisTemplate.opsForSet().members(DIRTY_SET_KEY);
        if (dirtyIds == null || dirtyIds.isEmpty()) {
            return;
        }
        for (String idStr : dirtyIds) {
            try {
                String pendingKey = PENDING_KEY_PREFIX + idStr;
                String deltaStr = redisTemplate.opsForValue().get(pendingKey); // 읽기만 한다(삭제 X).
                long delta = deltaStr == null ? 0L : Long.parseLong(deltaStr);
                if (delta <= 0) {
                    // 더할 게 없으면 dirty 표시만 정리한다.
                    redisTemplate.opsForSet().remove(DIRTY_SET_KEY, idStr);
                    continue;
                }

                // 1) DB부터 반영. 여기서 예외가 나면 아래 Redis는 손대지 않으므로 카운터가 그대로 남아 재시도된다.
                questionRepository.increaseViewCount(Long.parseLong(idStr), delta);

                // 2) 반영에 성공한 만큼만 차감. flush 도중 새로 들어온 조회분은 카운터에 남아 사라지지 않는다.
                Long remaining = redisTemplate.opsForValue().decrement(pendingKey, delta);
                if (remaining != null && remaining <= 0) {
                    redisTemplate.opsForSet().remove(DIRTY_SET_KEY, idStr);
                }
            } catch (Exception e) {
                // 이 질문만 건너뛰고 다음 주기에 다시 시도한다. 한 건 실패가 나머지 반영을 막지 않게 한다.
                log.warn("워키 조회수 flush 실패. questionId={} 는 다음 주기에 재시도한다.", idStr, e);
            }
        }
    }
}
