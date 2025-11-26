package org.ecospace.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.ecospace.model.SubscriptionType;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
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
    @NotBlank(message = "Field can't be empty!")
    private String description;
}
