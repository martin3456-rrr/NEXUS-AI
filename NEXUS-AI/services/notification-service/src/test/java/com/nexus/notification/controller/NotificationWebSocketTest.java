package com.nexus.notification.controller;

import com.nexus.notification.model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NotificationWebSocketTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private StompSession stompSession;

    private final CompletableFuture<Notification> publicNotificationFuture = new CompletableFuture<>();
    private final CompletableFuture<Notification> privateNotificationFuture = new CompletableFuture<>();

    @BeforeEach
    public void setup() throws Exception {
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);
        this.stompClient = new WebSocketStompClient(sockJsClient);
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        String wsUrl = String.format("ws://localhost:%d/ws", port);

        stompSession = this.stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);
    }

    @Test
    public void testPublicNotificationBroadcast() throws Exception {
        // Given
        String topic = "/topic/public";
        String destination = "/app/broadcast";
        stompSession.subscribe(topic, new DefaultStompFrameHandler(publicNotificationFuture));

        Notification payload = new Notification();
        payload.setContent("Test public broadcast");
        payload.setFrom("System");

        // When
        stompSession.send(destination, payload);

        // Then
        Notification received = publicNotificationFuture.get(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.getContent()).isEqualTo("Test public broadcast");
        assertThat(received.getFrom()).isEqualTo("System");
    }

    @Test
    public void testPrivateNotification() throws Exception {
        // Given
        String userId = "user-private-test";
        String queue = "/user/" + userId + "/queue/reply";
        String destination = "/app/private";
        stompSession.subscribe(queue, new DefaultStompFrameHandler(privateNotificationFuture));

        Notification payload = new Notification();
        payload.setContent("Test private message");
        payload.setTo(userId);

        // When
        stompSession.send(destination, payload);

        // Then
        Notification received = privateNotificationFuture.get(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.getContent()).isEqualTo("Test private message");
        assertThat(received.getTo()).isEqualTo(userId);
    }

    // Pomocniczy handler do odbierania wiadomości
    private static class DefaultStompFrameHandler implements StompFrameHandler {
        private final CompletableFuture<Notification> future;

        public DefaultStompFrameHandler(CompletableFuture<Notification> future) {
            this.future = future;
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return Notification.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            future.complete((Notification) payload);
        }
    }
}
