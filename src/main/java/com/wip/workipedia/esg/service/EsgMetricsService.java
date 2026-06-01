package com.wip.workipedia.esg.service;

import com.wip.workipedia.esg.dto.EsgMetricsResponse;
import org.springframework.stereotype.Service;

@Service
public class EsgMetricsService {

	public EsgMetricsResponse getMyMetrics() {
		return new EsgMetricsResponse(0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0);
	}

	public EsgMetricsResponse getAdminMetrics() {
		return new EsgMetricsResponse(0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0);
	}
}
