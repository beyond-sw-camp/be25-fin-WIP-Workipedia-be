package com.wip.workipedia.admin.repository;

import com.wip.workipedia.admin.domain.ManualVersion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManualVersionRepository extends JpaRepository<ManualVersion, Long> {

    Optional<ManualVersion> findTopByManualManualIdAndDeletedAtIsNullOrderByManualVersionIdDesc(Long manualId);

    boolean existsByManualManualIdAndManualNumAndDeletedAtIsNull(Long manualId, String manualNum);
}
