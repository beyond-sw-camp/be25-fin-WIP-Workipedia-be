package com.wip.workipedia.manual.domain;

import com.wip.workipedia.common.domain.BaseTimeEntity;
import com.wip.workipedia.common.domain.ModifiedSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "manuals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Manual extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manual_id")
    private Long manualId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ManualStatus status;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "file_key", length = 500)
    private String fileKey;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "created_by")
    private Long createdBy;

    private Manual(Long departmentId, String title, String description, String content, ManualStatus status,
            String sourceUrl, String version, Long createdBy) {
        this.departmentId = departmentId;
        this.title = title;
        this.description = description;
        this.content = content;
        this.status = status == null ? ManualStatus.PUBLISHED : status;
        this.sourceUrl = sourceUrl;
        this.version = version;
        this.createdBy = createdBy;
        touchModifiedSource(ModifiedSource.ADMIN);
    }

    public static Manual create(Long departmentId, String title, String content, ManualStatus status,
            String sourceUrl, String version, Long createdBy) {
        return create(departmentId, title, null, content, status, sourceUrl, version, createdBy);
    }

    public static Manual create(Long departmentId, String title, String description, String content, ManualStatus status,
            String sourceUrl, String version, Long createdBy) {
        return new Manual(departmentId, title, description, content, status, sourceUrl, version, createdBy);
    }

    public void update(Long departmentId, String title, String content, ManualStatus status,
            String sourceUrl, String version) {
        update(departmentId, title, null, content, status, sourceUrl, version);
    }

    public void update(Long departmentId, String title, String description, String content, ManualStatus status,
            String sourceUrl, String version) {
        if (departmentId != null) {
            this.departmentId = departmentId;
        }
        if (title != null) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        if (content != null) {
            this.content = content;
        }
        if (status != null) {
            this.status = status;
        }
        if (sourceUrl != null) {
            this.sourceUrl = sourceUrl;
        }
        if (version != null) {
            this.version = version;
        }
        touchModifiedSource(ModifiedSource.ADMIN);
    }

    // R2에 업로드된 원본 PDF를 매뉴얼에 연결한다.
    public void attachFile(String fileKey, String fileUrl) {
        this.fileKey = fileKey;
        this.fileUrl = fileUrl;
    }

    public void delete() {
        this.status = ManualStatus.DELETED;
        this.fileKey = null;
        this.fileUrl = null;
        markDeleted();
        touchModifiedSource(ModifiedSource.ADMIN);
    }
}
