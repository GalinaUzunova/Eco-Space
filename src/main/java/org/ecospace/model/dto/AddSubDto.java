package org.ecospace.model.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.ecospace.model.SubscriptionType;


@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class AddSubDto {

    @NotNull(message = "Choose Type!")
    private SubscriptionType type;

    @NotBlank(message = "Choose a name for the package!")
    private String namePackage;
    @NotNull(message = "Field can't be empty!")
    @Positive(message = "Price must be a positive number!")
    private Double price;

    @NotBlank(message = "Field can't be empty!")
    private String description;
}
