package com.nexus.notification.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private String title;
    private String message;
    private String recipient; // Może być nazwa użytkownika lub ID
    private String from;
    private String to; // "all" for broadcast or a specific username
    private String content;
    private long timestamp;
}