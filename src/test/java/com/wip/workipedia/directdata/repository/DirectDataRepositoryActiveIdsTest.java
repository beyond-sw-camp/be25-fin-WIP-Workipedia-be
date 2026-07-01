package com.wip.workipedia.directdata.repository;

import com.wip.workipedia.directdata.domain.DirectData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DirectDataRepositoryActiveIdsTest {

    @Autowired DirectDataRepository repository;

    @Test
    @DisplayName("findActiveIds는 비활성(is_active='N')을 제외하고 활성 ID만 포함한다")
    void findActiveIds_returnsActiveOnly() {
        DirectData active = repository.save(DirectData.create("t-active", "c", "cat", true, 1L));
        DirectData inactive = repository.save(DirectData.create("t-inactive", "c", "cat", false, 1L));
        repository.flush();

        List<Long> ids = repository.findActiveIds();

        assertThat(ids).contains(active.getDirectDataId());
        assertThat(ids).doesNotContain(inactive.getDirectDataId());
    }
}
