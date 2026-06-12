package com.wip.workipedia.manual.repository;

import com.wip.workipedia.manual.domain.ManualFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManualFileRepository extends JpaRepository<ManualFile, Long> {

    List<ManualFile> findByManualManualIdAndDeletedAtIsNullOrderBySortOrderAsc(Long manualId);

    long countByManualManualIdAndDeletedAtIsNull(Long manualId);
}
