package com.wip.workipedia.directdata.repository;

import com.wip.workipedia.directdata.domain.DirectData;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DirectDataRepository extends JpaRepository<DirectData, Long>,
        JpaSpecificationExecutor<DirectData> {

    Optional<DirectData> findByDirectDataIdAndDeletedAtIsNull(Long directDataId);

    boolean existsByDirectDataIdAndDeletedAtIsNotNull(Long directDataId);
}
