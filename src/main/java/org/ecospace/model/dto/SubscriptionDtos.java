package org.ecospace.model.dto;


import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter


public class SubscriptionDtos {
    @NotNull(message = "Choose subscription")
    private UUID id;

}
