package org.ecospace.model.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class SubscriptionDtos {
    @NotBlank(message = "Choose subscription")
    private UUID id;

}
