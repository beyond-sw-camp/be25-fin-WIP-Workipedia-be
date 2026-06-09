package com.wip.workipedia.point.service;

import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.point.dto.MyPointResponse;
import com.wip.workipedia.point.dto.PointHistoryResponse;
import com.wip.workipedia.point.repository.PointHistoryRepository;
import com.wip.workipedia.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {
	private final UserPointRepository userPointRepository;
	private final PointHistoryRepository pointHistoryRepository;

	@Transactional(readOnly = true)
	public MyPointResponse getMyPoint(Long userId) {
		return userPointRepository.findByUserIdAndDeletedAtIsNull(userId)
			.map(MyPointResponse::from)
			.orElseGet(() -> new MyPointResponse(userId, 0L));
	}

	@Transactional(readOnly = true)
	public PageResponse<PointHistoryResponse> getMyPointHistory(Long userId, Pageable pageable) {
		return PageResponse.from(
			pointHistoryRepository
				.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId, pageable)
				.map(PointHistoryResponse::from)
		);
	}
}
