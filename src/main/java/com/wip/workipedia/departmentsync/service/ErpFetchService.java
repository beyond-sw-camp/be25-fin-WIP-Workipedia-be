package com.wip.workipedia.departmentsync.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.departmentsync.dto.ErpFetchResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class ErpFetchService {

	private final ObjectMapper objectMapper;
	private final RestClient restClient;

	public ErpFetchService(ObjectMapper objectMapper, RestClient.Builder builder) {
		this.objectMapper = objectMapper;
		this.restClient = builder == null ? null : builder.build();
	}

	// ERP 부서 API URL을 BE가 직접 GET 호출(브라우저 CORS 회피)해 JSON을 columns/rows로 평탄화한다.
	public ErpFetchResponse fetch(String url) {
		try {
			String body = restClient.get().uri(url).retrieve().body(String.class);
			return parse(body);
		} catch (CustomException ce) {
			throw ce;
		} catch (Exception e) {
			log.warn("[ERP-FETCH] 실패: url={}, err={}", url, e.getMessage());
			throw new CustomException(ErrorType.INTERNAL_ERROR);
		}
	}

	public ErpFetchResponse parse(String json) {
		try {
			JsonNode root = objectMapper.readTree(json);
			JsonNode arr = root.isArray() ? root : root.path("data");
			if (!arr.isArray()) {
				throw new CustomException(ErrorType.INTERNAL_ERROR);
			}

			Set<String> columns = new LinkedHashSet<>();
			List<Map<String, String>> rows = new ArrayList<>();
			for (JsonNode node : arr) {
				Map<String, String> row = new LinkedHashMap<>();
				node.fieldNames().forEachRemaining(field -> {
					columns.add(field);
					JsonNode value = node.get(field);
					row.put(field, value == null || value.isNull() ? null : value.asText());
				});
				rows.add(row);
			}
			return new ErpFetchResponse(new ArrayList<>(columns), rows);
		} catch (CustomException ce) {
			throw ce;
		} catch (Exception e) {
			throw new CustomException(ErrorType.INTERNAL_ERROR);
		}
	}
}
