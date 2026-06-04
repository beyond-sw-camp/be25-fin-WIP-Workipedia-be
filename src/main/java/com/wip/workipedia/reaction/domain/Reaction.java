package com.wip.workipedia.reaction.domain;

import com.wip.workipedia.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "reactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reaction_id")
    private Long reactionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private ReactionTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false, length = 30)
    private ReactionType reactionType;

    private Reaction(Long userId, ReactionTargetType targetType, Long targetId, ReactionType reactionType) {
        this.userId = userId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reactionType = reactionType;
    }

    public static Reaction create(Long userId, ReactionTargetType targetType, Long targetId, ReactionType reactionType) {
        return new Reaction(userId, targetType, targetId, reactionType);
    }
}
