package org.ecospace.notification.client.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ContactResponse {

    private UUID id;
    private String name;
    private String email;
    private String status;
    private LocalDateTime createdAt;
}
