package org.ecospace.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@Getter@Setter
@AllArgsConstructor

public class SubscriptionRenewEvent {

    private UUID userId;
    private String username;
    private String email;
    private String subscriptionName;
    private Double price;
    private LocalDateTime expiredOn;
}
