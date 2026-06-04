package com.wip.workipedia.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.Getter;

// 전역 @EnableJpaAuditing(Step1, 타 담당)에 의존하지 않도록 lifecycle 콜백으로 시간 컬럼을 채운다.
// 모든 도메인 엔티티가 상속하는 JPA 베이스 클래스(@MappedSuperclass). 시간/소프트삭제/수정주체 컬럼을 공통으로 제공한다.
@Getter
@MappedSuperclass
public abstract class BaseTimeEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 마지막 수정 주체. 사용자/관리자/챗봇/시스템 변경의 출처 추적용.
    @Enumerated(EnumType.STRING)
    @Column(name = "modified_source", length = 30)
    private ModifiedSource modifiedSource;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void markDeleted() {
        this.deletedAt = LocalDateTime.now();
    }

    public void touchModifiedSource(ModifiedSource source) {
        this.modifiedSource = source;
    }
}
