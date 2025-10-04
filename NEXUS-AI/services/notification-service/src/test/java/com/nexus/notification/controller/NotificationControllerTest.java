package com.nexus.notification.controller;

import com.nexus.notification.model.Notification;
import org.testng.annotations.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.verify;

@SpringBootTest
class NotificationControllerTest {

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void shouldSendNotificationToUser() {
        // Given
        NotificationController controller = new NotificationController();
        // Set the messaging template via reflection or setter if available

        Notification notification = new Notification("Test Title", "Test Message", "testuser");

        // When
        controller.sendNotificationToUser("testuser", notification);

        // Then
        verify(messagingTemplate).convertAndSendToUser("testuser", "/topic/notifications", notification);
    }
}
