package com.wip.workipedia.departmentsync.dto;

import java.util.List;
import java.util.Map;

public record ErpFetchResponse(List<String> columns, List<Map<String, String>> rows) {}
