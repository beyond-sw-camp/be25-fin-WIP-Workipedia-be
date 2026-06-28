package com.wip.workipedia.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class NotificationSseServiceTest {

    private final NotificationSseService service = new NotificationSseService();

    @Test
    void subscribe_registersConnection() {
        SseEmitter emitter = service.subscribe(1L);

        assertThat(emitter).isNotNull();
        assertThat(service.activeConnections(1L)).isEqualTo(1);
    }

    @Test
    void subscribe_sameUserTwice_countsBoth() {
        service.subscribe(1L);
        service.subscribe(1L);

        assertThat(service.activeConnections(1L)).isEqualTo(2);
    }

    @Test
    void send_toSubscribedUser_doesNotThrow() {
        service.subscribe(1L);

        assertThatCode(() -> service.send(1L, new Object())).doesNotThrowAnyException();
    }

    @Test
    void send_toUnknownUser_isNoOp() {
        assertThatCode(() -> service.send(999L, new Object())).doesNotThrowAnyException();
        assertThat(service.activeConnections(999L)).isEqualTo(0);
    }

    @Test
    void heartbeat_withNoConnections_doesNotThrow() {
        assertThatCode(service::heartbeat).doesNotThrowAnyException();
    }
}
