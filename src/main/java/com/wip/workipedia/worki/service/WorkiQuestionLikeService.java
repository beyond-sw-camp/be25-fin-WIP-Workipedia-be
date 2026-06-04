package com.wip.workipedia.worki.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.reaction.domain.Reaction;
import com.wip.workipedia.reaction.domain.ReactionTargetType;
import com.wip.workipedia.reaction.domain.ReactionType;
import com.wip.workipedia.reaction.repository.ReactionRepository;
import com.wip.workipedia.worki.repository.WorkiQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkiQuestionLikeService {

    private final ReactionRepository reactionRepository;
    private final WorkiQuestionRepository questionRepository;

    public void like(Long actorUserId, Long questionId) {
        ensureQuestionExists(questionId);
        reactionRepository.findByUserIdAndTargetTypeAndTargetIdAndReactionType(
                actorUserId, ReactionTargetType.WORKI_QUESTION, questionId, ReactionType.LIKE)
                .ifPresent(r -> {
                    throw new CustomException(ErrorType.WORKI_POLICY_VIOLATION, "이미 좋아요를 누른 질문입니다.");
                });
        reactionRepository.save(Reaction.create(
                actorUserId, ReactionTargetType.WORKI_QUESTION, questionId, ReactionType.LIKE));
    }

    public void unlike(Long actorUserId, Long questionId) {
        ensureQuestionExists(questionId);
        Reaction reaction = reactionRepository
                .findByUserIdAndTargetTypeAndTargetIdAndReactionType(
                        actorUserId, ReactionTargetType.WORKI_QUESTION, questionId, ReactionType.LIKE)
                .orElseThrow(() -> new CustomException(ErrorType.WORKI_NOT_FOUND, "좋아요 기록이 없습니다."));
        reactionRepository.delete(reaction);
    }

    private void ensureQuestionExists(Long questionId) {
        questionRepository.findByQuestionIdAndDeletedAtIsNull(questionId)
                .orElseThrow(() -> new CustomException(ErrorType.WORKI_NOT_FOUND, "질문을 찾을 수 없습니다. id=" + questionId));
    }
}
