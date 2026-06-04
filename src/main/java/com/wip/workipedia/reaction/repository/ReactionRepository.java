package com.wip.workipedia.reaction.repository;

import com.wip.workipedia.reaction.domain.Reaction;
import com.wip.workipedia.reaction.domain.ReactionTargetType;
import com.wip.workipedia.reaction.domain.ReactionType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    Optional<Reaction> findByUserIdAndTargetTypeAndTargetIdAndReactionType(
            Long userId, ReactionTargetType targetType, Long targetId, ReactionType reactionType);

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
