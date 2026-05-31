package com.wip.workipedia.common.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class BasePageRequest {

	@Min(1)
	private int page = 1;

	@Min(1)
	@Max(100)
	private int size = 10;

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Pageable toPageable() {
		return PageRequest.of(page - 1, size);
	}

	public Pageable toPageable(Sort sort) {
		return PageRequest.of(page - 1, size, sort);
	}
}
