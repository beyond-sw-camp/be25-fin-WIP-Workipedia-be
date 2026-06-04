package com.wip.workipedia.point.service;

import com.wip.workipedia.point.dto.MyPointResponse;
import com.wip.workipedia.point.dto.PointHistoryResponse;
import com.wip.workipedia.point.repository.PointHistoryRepository;
import com.wip.workipedia.point.repository.UserPointRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {
	private static final Long SKELETON_USER_ID = 1L; // 임시

	private final UserPointRepository userPointRepository;
	private final PointHistoryRepository pointHistoryRepository;

	@Transactional(readOnly = true)
	public MyPointResponse getMyPoint() {
		return userPointRepository.findByUserIdAndDeletedAtIsNull(SKELETON_USER_ID)
			.map(MyPointResponse::from)
			.orElseGet(() -> new MyPointResponse(SKELETON_USER_ID, 0L));
	}

	@Transactional(readOnly = true)
	public List<PointHistoryResponse> getMyPointHistory() {
		return pointHistoryRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(SKELETON_USER_ID).stream()
			.map(PointHistoryResponse::from)
			.toList();
	}
}
