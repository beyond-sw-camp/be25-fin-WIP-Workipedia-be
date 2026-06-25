package com.wip.workipedia.manual.repository;

import com.wip.workipedia.manual.domain.ManualPage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManualPageRepository extends JpaRepository<ManualPage, Long> {

    List<ManualPage> findByManualManualIdAndDeletedAtIsNullOrderByFileSortOrderAscPageNumberAsc(Long manualId);
}
