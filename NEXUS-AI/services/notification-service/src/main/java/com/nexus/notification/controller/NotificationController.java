package com.nexus.notification.controller;

import com.nexus.notification.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/notify")
    @SendTo("/topic/notifications")
    public Notification sendNotification(@Payload String message) {
        logger.info("Simple notification broadcast: {}", message);

        Notification notification = new Notification();
        notification.setContent("New Notification: " + message);
        notification.setTimestamp(System.currentTimeMillis());
        notification.setTo("public");

        return notification;
    }

    @MessageMapping("/broadcast")
    @SendTo("/topic/public")
    public Notification sendPublicNotification(@Payload Notification notification) {
        logger.info("Broadcasting public notification: {}", notification.getContent());
        notification.setTimestamp(System.currentTimeMillis());
        return notification;
    }

    @MessageMapping("/private")
    public void sendPrivateNotification(@Payload Notification notification) {
        logger.info("Sending private notification to {}: {}", notification.getTo(), notification.getContent());
        notification.setTimestamp(System.currentTimeMillis());
        messagingTemplate.convertAndSendToUser(notification.getTo(), "/queue/reply", notification);
    }
}
