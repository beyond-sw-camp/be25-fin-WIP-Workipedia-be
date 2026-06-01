package com.wip.workipedia.common.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class PageResponseTest {

	@Test
	void createsPageResponseFromSpringPage() {
		Page<String> page = new PageImpl<>(
			List.of("a", "b"),
			PageRequest.of(0, 2),
			5
		);

		PageResponse<String> response = PageResponse.from(page);

		assertThat(response.content()).containsExactly("a", "b");
		assertThat(response.pageInfo().page()).isEqualTo(1);
		assertThat(response.pageInfo().size()).isEqualTo(2);
		assertThat(response.pageInfo().totalElements()).isEqualTo(5);
		assertThat(response.pageInfo().totalPages()).isEqualTo(3);
		assertThat(response.pageInfo().hasNext()).isTrue();
		assertThat(response.pageInfo().hasPrevious()).isFalse();
	}
}
