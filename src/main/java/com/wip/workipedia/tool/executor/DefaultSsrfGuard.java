package com.wip.workipedia.tool.executor;

import com.wip.workipedia.config.ToolAllowedHostProperties;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DefaultSsrfGuard implements SsrfGuard {

	private final Set<String> allowedHosts;

	public DefaultSsrfGuard(ToolAllowedHostProperties properties) {
		List<String> configured = properties.allowedHosts();
		this.allowedHosts = (configured == null ? List.<String>of() : configured)
			.stream()
			.filter(host -> host != null && !host.isBlank())
			.map(String::toLowerCase)
			.collect(Collectors.toSet());
	}

	@Override
	public boolean isSafe(String endpointUrl) {
		try {
			URI uri = URI.create(endpointUrl);
			if (!"https".equalsIgnoreCase(uri.getScheme())) {
				return false;
			}

			String host = uri.getHost();
			if (host == null || !allowedHosts.contains(host.toLowerCase())) {
				return false;
			}

			InetAddress address = InetAddress.getByName(host);
			return !(address.isLoopbackAddress()
				|| address.isLinkLocalAddress()
				|| address.isSiteLocalAddress()
				|| address.isAnyLocalAddress()
				|| address.isMulticastAddress());
		} catch (UnknownHostException | IllegalArgumentException e) {
			return false;
		}
	}
}
