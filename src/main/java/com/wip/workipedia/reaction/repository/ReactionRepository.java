package com.wip.workipedia.reaction.repository;

import com.wip.workipedia.reaction.domain.Reaction;
import com.wip.workipedia.reaction.domain.ReactionTargetType;
import com.wip.workipedia.reaction.domain.ReactionType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    Optional<Reaction> findByUserIdAndTargetTypeAndTargetIdAndReactionType(
            Long userId, ReactionTargetType targetType, Long targetId, ReactionType reactionType);
}
