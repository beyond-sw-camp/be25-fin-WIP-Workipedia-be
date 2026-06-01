package com.wip.workipedia.point.service;

import com.wip.workipedia.point.dto.MyPointResponse;
import com.wip.workipedia.point.dto.PointHistoryResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PointService {

	public MyPointResponse getMyPoint() {
		return new MyPointResponse(0L, 0L, 0L);
	}

	public List<PointHistoryResponse> getMyPointHistory() {
		return List.of();
	}
}
