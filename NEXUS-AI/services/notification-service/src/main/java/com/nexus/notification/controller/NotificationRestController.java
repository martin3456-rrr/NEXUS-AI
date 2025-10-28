package com.nexus.notification.controller;

import com.nexus.notification.dto.CreateNotificationRequest;
import com.nexus.notification.entity.NotificationEntity;
import com.nexus.notification.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Validated
public class NotificationRestController {

    private final NotificationService service;

    public NotificationRestController(NotificationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<NotificationEntity> create(@RequestBody @Validated CreateNotificationRequest req) {
        NotificationEntity created = service.create(req.userId(), req.content(), req.type());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{userId}")
    public List<NotificationEntity> list(@PathVariable String userId) {
        return service.list(userId);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        service.markRead(id);
        return ResponseEntity.noContent().build();
    }
}
