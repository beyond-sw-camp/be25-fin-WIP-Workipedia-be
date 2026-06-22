package com.wip.workipedia.tool.repository;

import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.domain.ApprovalStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiToolRepository extends JpaRepository<AiTool, Long> {
	List<AiTool> findByIsActiveAndApprovalStatusAndIsDeleted(
		String isActive, ApprovalStatus approvalStatus, String isDeleted
	);

	Optional<AiTool> findByAiToolIdAndIsDeleted(Long aiToolId, String isDeleted);

	Page<AiTool> findByIsDeleted(String isDeleted, Pageable pageable);
}
