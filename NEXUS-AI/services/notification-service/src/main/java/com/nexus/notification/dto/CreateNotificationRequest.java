package com.nexus.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateNotificationRequest(
        @NotBlank String userId,
        @NotBlank String content,
        @NotBlank String type
) {}
