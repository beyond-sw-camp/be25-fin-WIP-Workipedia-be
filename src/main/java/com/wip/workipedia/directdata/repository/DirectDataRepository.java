package com.wip.workipedia.directdata.repository;

import com.wip.workipedia.directdata.domain.DirectData;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface DirectDataRepository extends JpaRepository<DirectData, Long>,
        JpaSpecificationExecutor<DirectData> {

    Optional<DirectData> findByDirectDataIdAndDeletedAtIsNull(Long directDataId);

    boolean existsByDirectDataIdAndDeletedAtIsNotNull(Long directDataId);

    // 전체 재동기화용 — 활성(is_active='Y', 미삭제) 수기 지식 ID 전체
    @Query(value = """
        SELECT direct_data_id FROM direct_data
        WHERE deleted_at IS NULL AND is_active = 'Y' AND is_deleted = 'N'
        """, nativeQuery = true)
    List<Long> findActiveIds();
}
