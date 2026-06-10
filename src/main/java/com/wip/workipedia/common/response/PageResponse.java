package com.wip.workipedia.common.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(
	List<T> content,
	PageInfo pageInfo
) {

	public static <T> PageResponse<T> from(Page<T> page) {
		return new PageResponse<>(
			page.getContent(),
			new PageInfo(
				page.getNumber() + 1,
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.hasNext(),
				page.hasPrevious()
			)
		);
	}

	public record PageInfo(
		int page,
		int size,
		long totalElements,
		int totalPages,
		boolean hasNext,
		boolean hasPrevious
	) {
	}
}
