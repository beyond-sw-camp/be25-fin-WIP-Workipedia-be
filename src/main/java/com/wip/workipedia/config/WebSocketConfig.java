package com.wip.workipedia.config;

import com.wip.workipedia.common.security.JwtProvider;
import com.wip.workipedia.user.domain.UserRole;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/flash-chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        registry.addEndpoint("/ws/flash-chat-native")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic");
    }

    // 연결 인증 하는 메서드
    @Override
    public void configureClientInboundChannel(
            org.springframework.messaging.simp.config.ChannelRegistration registration) {
                // 메세지 가로채서 확인
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                // 메세지를 보고 새 복사본 accessor를 만듦. 원본은 버리게 됨.
                // StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                // 위의 코드는 원본을 버린다면, 이건 원본에서 정보를 가져와서 로그인됨을 나타냄.
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                // 메세지 무슨 명령인지 확인.
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    authenticate(accessor);
                }
                return message;
            }
        });
    }

    // 토큰 확인
    private void authenticate(StompHeaderAccessor accessor) {
        String accessToken = resolveAccessToken(accessor);
        if (accessToken == null || !jwtProvider.isValidAccessToken(accessToken)) {
            throw new MessageDeliveryException("Unauthorized: valid JWT token required");
        }

        Long userId = jwtProvider.getUserId(accessToken);
        UserRole role = jwtProvider.getRole(accessToken);
        accessor.setUser(new StompUserPrincipal(userId, role));
    }

    // 토큰 문자열 추출
    private String resolveAccessToken(StompHeaderAccessor accessor) {
        List<String> authorizationHeaders = accessor.getNativeHeader("Authorization");
        if (authorizationHeaders == null || authorizationHeaders.isEmpty()) {
            return null;
        }

        String authorizationHeader = authorizationHeaders.get(0);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorizationHeader.substring(BEARER_PREFIX.length());
    }

    private record StompUserPrincipal(Long userId, UserRole role) implements java.security.Principal {
        @Override
        public String getName() {
            return userId.toString();
        }
    }
}
