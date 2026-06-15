package com.wip.workipedia.directdata.domain;

import com.wip.workipedia.common.domain.BaseTimeEntity;
import com.wip.workipedia.common.domain.ModifiedSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "direct_data")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectData extends BaseTimeEntity {

    private static final String YES = "Y";
    private static final String NO = "N";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "direct_data_id")
    private Long directDataId;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(length = 100)
    private String category;

    @Column(name = "is_active", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String isActive = YES;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "is_deleted", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String isDeleted = NO;

    private DirectData(String title, String content, String category, boolean active, Long createdBy) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.isActive = toYn(active);
        this.createdBy = createdBy;
        touchModifiedSource(ModifiedSource.ADMIN);
    }

    public static DirectData create(String title, String content, String category, boolean active, Long createdBy) {
        return new DirectData(title, content, category, active, createdBy);
    }

    public void update(String title, String content, String category, boolean active, Long updatedBy) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.isActive = toYn(active);
        this.updatedBy = updatedBy;
        touchModifiedSource(ModifiedSource.ADMIN);
    }

    public void delete(Long updatedBy) {
        markDeleted();
        this.isDeleted = YES;
        this.updatedBy = updatedBy;
        touchModifiedSource(ModifiedSource.ADMIN);
    }

    public boolean isActive() {
        return YES.equals(isActive);
    }

    private static String toYn(boolean active) {
        return active ? YES : NO;
    }
}
