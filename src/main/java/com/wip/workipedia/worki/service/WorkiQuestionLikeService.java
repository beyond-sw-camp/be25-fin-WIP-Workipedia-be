package com.wip.workipedia.worki.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.reaction.domain.Reaction;
import com.wip.workipedia.reaction.domain.ReactionTargetType;
import com.wip.workipedia.reaction.domain.ReactionType;
import com.wip.workipedia.reaction.repository.ReactionRepository;
import com.wip.workipedia.worki.repository.WorkiQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkiQuestionLikeService {

    private final ReactionRepository reactionRepository;
    private final WorkiQuestionRepository questionRepository;
    // TOCTOU 방지: 사전 조회 없이 바로 INSERT 시도 → DB UNIQUE 제약(uk_reactions_user_target)이 동시성 보장.
    // 동시 두 번 호출 시 한쪽만 성공, 다른 쪽은 DataIntegrityViolationException → 409로 변환.
    public void like(Long actorUserId, Long questionId) {
        ensureQuestionExists(questionId);
        try {
            reactionRepository.save(Reaction.create(
                    actorUserId, ReactionTargetType.WORKI_QUESTION, questionId, ReactionType.LIKE));
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorType.WORKI_POLICY_VIOLATION, "이미 좋아요를 누른 질문입니다.");
        }
    }

    // TOCTOU 방지: 사전 조회 후 delete 대신 단일 DELETE 쿼리. 영향받은 행 수로 결과 판정 → 동시 호출 시 한쪽만 204, 다른 쪽은 404.
    public void unlike(Long actorUserId, Long questionId) {
        ensureQuestionExists(questionId);
        int deleted = reactionRepository.deleteByUserAndTarget(
                actorUserId, ReactionTargetType.WORKI_QUESTION, questionId, ReactionType.LIKE);
        if (deleted == 0) {
            throw new CustomException(ErrorType.WORKI_NOT_FOUND, "좋아요 기록이 없습니다.");
        }
    }

    private void ensureQuestionExists(Long questionId) {
        questionRepository.findByQuestionIdAndDeletedAtIsNull(questionId)
                .orElseThrow(() -> new CustomException(ErrorType.WORKI_NOT_FOUND, "질문을 찾을 수 없습니다. id=" + questionId));
    }

   
}
