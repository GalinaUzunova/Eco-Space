package org.ecospace.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ecospace.model.SubscriptionType;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EditSubDto {
  @NotNull
   private UUID id;
    @NotNull(message="Field can't be empty")
    private SubscriptionType type;
    @NotBlank(message = "Choose a name for the package!")
    private String namePackage;
    @NotNull(message = " Filed can't be empty")
    @Positive(message = "Price must be a positive number!")
    private double price;
    @NotBlank(message = "Subscription period is required!")
    private String subscriptionPeriod;
    @NotBlank(message = "Field can't be empty!")
    private String description;
}
