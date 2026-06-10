package com.wip.workipedia.reaction.repository;

import com.wip.workipedia.reaction.domain.Reaction;
import com.wip.workipedia.reaction.domain.ReactionTargetType;
import com.wip.workipedia.reaction.domain.ReactionType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    Optional<Reaction> findByUserIdAndTargetTypeAndTargetIdAndReactionType(
            Long userId, ReactionTargetType targetType, Long targetId, ReactionType reactionType);

    // 특정 대상(질문/답변)의 특정 반응(LIKE 등) 개수. reactions는 하드 삭제라 행 수가 곧 현재 개수다.
    long countByTargetTypeAndTargetIdAndReactionType(
            ReactionTargetType targetType, Long targetId, ReactionType reactionType);

    // 여러 대상의 반응 개수를 target_id로 묶어 한 번에 집계한다(목록의 N+1 방지). 반응이 0건인 대상은 결과에 없다.
    @Query("""
            SELECT r.targetId AS targetId, COUNT(r) AS likeCount
              FROM Reaction r
             WHERE r.targetType = :targetType
               AND r.reactionType = :reactionType
               AND r.targetId IN :targetIds
             GROUP BY r.targetId
            """)
    List<TargetLikeCount> countLikesByTargetIds(@Param("targetType") ReactionTargetType targetType,
                                                @Param("reactionType") ReactionType reactionType,
                                                @Param("targetIds") List<Long> targetIds);

    // 단일 DELETE 쿼리로 원자적 처리. 동시 요청 시 두 번째 호출은 deleted=0이 되어 호출자가 404로 분기 가능.
    @Modifying
    @Query("DELETE FROM Reaction r "
            + "WHERE r.userId = :userId "
            + "AND r.targetType = :targetType "
            + "AND r.targetId = :targetId "
            + "AND r.reactionType = :reactionType")
    int deleteByUserAndTarget(@Param("userId") Long userId,
                              @Param("targetType") ReactionTargetType targetType,
                              @Param("targetId") Long targetId,
                              @Param("reactionType") ReactionType reactionType);
}
