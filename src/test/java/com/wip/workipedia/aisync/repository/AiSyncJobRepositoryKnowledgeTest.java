package com.wip.workipedia.aisync.repository;

import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AiSyncJobRepositoryKnowledgeTest {

    @Autowired AiSyncJobRepository repository;

    private static final List<String> KNOWLEDGE = List.of("KNOWLEDGE_DATA", "MANUAL_KNOWLEDGE");

    // 공유 workipedia_test DB의 기존 데이터와 충돌하지 않도록 높은 source_id 사용
    private static final long BASE_ID = 900_000_000L;

    @Test
    @DisplayName("countPendingBySourceTypes는 스코프 PENDING만 델타로 센다")
    void countPending_scoped() {
        long before = repository.countPendingBySourceTypes(KNOWLEDGE);

        repository.save(AiSyncJob.create(AiSyncSourceType.KNOWLEDGE_DATA, BASE_ID + 1, AiSyncOperation.UPSERT));
        repository.save(AiSyncJob.create(AiSyncSourceType.MANUAL_KNOWLEDGE, BASE_ID + 2, AiSyncOperation.DELETE));
        repository.save(AiSyncJob.create(AiSyncSourceType.WORKI, BASE_ID + 3, AiSyncOperation.UPSERT)); // 스코프 밖
        repository.flush();

        long after = repository.countPendingBySourceTypes(KNOWLEDGE);

        assertThat(after - before).isEqualTo(2);
    }

    @Test
    @DisplayName("claimPendingKnowledgeJobs는 스코프 밖 타입을 가져오지 않는다")
    void claim_scopedOnly() {
        repository.save(AiSyncJob.create(AiSyncSourceType.KNOWLEDGE_DATA, BASE_ID + 11, AiSyncOperation.UPSERT));
        repository.flush();

        List<AiSyncJob> claimed =
            repository.claimPendingKnowledgeJobs(KNOWLEDGE, LocalDateTime.now(), 50);

        assertThat(claimed).isNotEmpty();
        assertThat(claimed).allSatisfy(j ->
            assertThat(j.getSourceType())
                .isIn(AiSyncSourceType.KNOWLEDGE_DATA, AiSyncSourceType.MANUAL_KNOWLEDGE));
    }

    @Test
    @DisplayName("countByStatusLatestForSourceTypes는 스코프 밖 타입을 제외한다(델타=1)")
    void statsScoped_excludesOthers() {
        long before = repository.countByStatusLatestForSourceTypes(KNOWLEDGE).stream()
            .mapToLong(AiSyncJobRepository.AiSyncStatusCount::getCount).sum();

        repository.save(AiSyncJob.create(AiSyncSourceType.KNOWLEDGE_DATA, BASE_ID + 21, AiSyncOperation.UPSERT));
        repository.save(AiSyncJob.create(AiSyncSourceType.WORKI, BASE_ID + 22, AiSyncOperation.UPSERT)); // 제외
        repository.flush();

        long after = repository.countByStatusLatestForSourceTypes(KNOWLEDGE).stream()
            .mapToLong(AiSyncJobRepository.AiSyncStatusCount::getCount).sum();

        assertThat(after - before).isEqualTo(1);
    }
}
