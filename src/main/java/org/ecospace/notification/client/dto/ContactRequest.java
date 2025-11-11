package org.ecospace.notification.client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContactRequest {
    private String name;
    private String email;
    private String phone;
    private String message;
    private String subject;
}
