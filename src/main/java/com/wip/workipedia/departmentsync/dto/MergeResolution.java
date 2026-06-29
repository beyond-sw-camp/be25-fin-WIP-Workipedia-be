package com.wip.workipedia.departmentsync.dto;

import java.util.List;

public record MergeResolution(List<String> fromExternalIds, String toExternalId, String rrMode) {}
