package com.nexus.notification.service;

import com.nexus.notification.entity.NotificationEntity;
import com.nexus.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public NotificationEntity create(String userId, String content, String type) {
        NotificationEntity n = new NotificationEntity();
        n.setUserId(userId);
        n.setContent(content);
        n.setType(type);
        n.setReadFlag(false);
        n.setCreatedAt(Instant.now());
        return repository.save(n);
    }

    public List<NotificationEntity> list(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void markRead(Long id) {
        NotificationEntity n = repository.findById(id)
                .orElseThrow(() -> new com.nexus.notification.exception.NotFoundException("Notification not found"));
        n.setReadFlag(true);
        repository.save(n);
    }
}
