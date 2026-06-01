package com.wip.workipedia.esg.service;

import com.wip.workipedia.esg.dto.EsgMetricsResponse;
import org.springframework.stereotype.Service;

@Service
public class EsgMetricsService {

	public EsgMetricsResponse getMyMetrics() {
		return new EsgMetricsResponse(12, 4, 60, 0.85, 0.72, 0.35, 0.82);
	}

	public EsgMetricsResponse getAdminMetrics() {
		return new EsgMetricsResponse(58, 21, 420, 0.78, 0.74, 0.41, 0.80);
	}
}
