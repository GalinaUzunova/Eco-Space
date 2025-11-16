package org.ecospace.notification.client.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ContactRequest {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String message;
    private String subject;
    private LocalDateTime createdAt;
}
