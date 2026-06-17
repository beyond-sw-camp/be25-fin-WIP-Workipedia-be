package com.wip.workipedia.tool.executor;

import com.wip.workipedia.config.ToolAllowedHostProperties;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultSsrfGuardTest {

	private DefaultSsrfGuard guardWithAllowedHosts(String... hosts) {
		return new DefaultSsrfGuard(new ToolAllowedHostProperties(List.of(hosts)));
	}

	@Test
	void isSafe_allowlist에_있고_HTTPS이면_true() {
		// hr.example.com은 실제 DNS에 존재하지 않아 InetAddress.getByName이 실패한다. example.com은 IANA 예약 도메인으로 실제 resolve된다.
		DefaultSsrfGuard ssrfGuard = guardWithAllowedHosts("example.com");

		assertThat(ssrfGuard.isSafe("https://example.com/api")).isTrue();
	}

	@Test
	void isSafe_allowlist에_없는_host는_차단() {
		DefaultSsrfGuard ssrfGuard = guardWithAllowedHosts("hr.example.com");

		assertThat(ssrfGuard.isSafe("https://other.example.com/api")).isFalse();
	}

	@Test
	void isSafe_allowlist가_비어있으면_전부_차단() {
		DefaultSsrfGuard ssrfGuard = guardWithAllowedHosts();

		assertThat(ssrfGuard.isSafe("https://hr.example.com/api")).isFalse();
	}

	@Test
	void isSafe_HTTP는_allowlist에_있어도_차단() {
		DefaultSsrfGuard ssrfGuard = guardWithAllowedHosts("hr.example.com");

		assertThat(ssrfGuard.isSafe("http://hr.example.com/api")).isFalse();
	}

	@Test
	void isSafe_allowlist에_사설_IP가_등록돼도_차단() {
		DefaultSsrfGuard ssrfGuard = guardWithAllowedHosts("192.168.1.1", "127.0.0.1", "169.254.169.254");

		assertThat(ssrfGuard.isSafe("https://192.168.1.1/api")).isFalse();
		assertThat(ssrfGuard.isSafe("https://127.0.0.1/api")).isFalse();
		assertThat(ssrfGuard.isSafe("https://169.254.169.254/latest/meta-data")).isFalse();
	}
}
