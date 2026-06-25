package com.wip.workipedia.manual.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "manual_pages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ManualPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manual_page_id")
    private Long manualPageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manual_id", nullable = false)
    private Manual manual;

    @Column(name = "file_key", nullable = false, length = 500)
    private String fileKey;

    @Column(name = "file_name", nullable = false, length = 500)
    private String fileName;

    @Column(name = "file_sort_order", nullable = false)
    private int fileSortOrder;

    @Column(name = "page_number", nullable = false)
    private int pageNumber;

    @Column(name = "global_page_number", nullable = false)
    private int globalPageNumber;

    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private ManualPage(Manual manual, String fileKey, String fileName, int fileSortOrder,
            int pageNumber, int globalPageNumber, String content) {
        this.manual = manual;
        this.fileKey = fileKey;
        this.fileName = fileName;
        this.fileSortOrder = fileSortOrder;
        this.pageNumber = pageNumber;
        this.globalPageNumber = globalPageNumber;
        this.content = content;
    }

    public static ManualPage create(Manual manual, String fileKey, String fileName, int fileSortOrder,
            int pageNumber, int globalPageNumber, String content) {
        return new ManualPage(manual, fileKey, fileName, fileSortOrder, pageNumber, globalPageNumber, content);
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

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
}
