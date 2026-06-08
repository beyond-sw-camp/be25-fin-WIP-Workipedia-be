package com.wip.workipedia.manual.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "manual_chunks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ManualChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manual_chunk_id")
    private Long manualChunkId;

    @Column(name = "manual_id", nullable = false)
    private Long manualId;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "embedding_json", columnDefinition = "JSON")
    private String embeddingJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private ManualChunk(Long manualId, int chunkIndex, String content) {
        this.manualId = manualId;
        this.chunkIndex = chunkIndex;
        this.content = content;
    }

    public static ManualChunk create(Long manualId, int chunkIndex, String content) {
        return new ManualChunk(manualId, chunkIndex, content);
    }

    public void replaceContent(String content) {
        this.content = content;
        this.embeddingJson = null;
        this.deletedAt = null;
    }

    public void markDeleted() {
        this.deletedAt = LocalDateTime.now();
        this.embeddingJson = null;
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
