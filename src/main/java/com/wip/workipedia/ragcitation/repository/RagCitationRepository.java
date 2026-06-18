package com.wip.workipedia.ragcitation.repository;

import com.wip.workipedia.ragcitation.domain.RagCitation;
import com.wip.workipedia.ragcitation.domain.RagCitationCitedByType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RagCitationRepository extends JpaRepository<RagCitation, Long> {

    void deleteByCitedByTypeAndCitedById(RagCitationCitedByType citedByType, Long citedById);
}
