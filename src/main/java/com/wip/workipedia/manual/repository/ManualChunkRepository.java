package com.wip.workipedia.manual.repository;

import com.wip.workipedia.manual.domain.ManualChunk;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManualChunkRepository extends JpaRepository<ManualChunk, Long> {

    List<ManualChunk> findByManualIdOrderByChunkIndexAsc(Long manualId);

    List<ManualChunk> findByManualIdAndDeletedAtIsNullOrderByChunkIndexAsc(Long manualId);
}
