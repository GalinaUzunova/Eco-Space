package org.ecospace.model.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ecospace.model.SubscriptionType;


@NoArgsConstructor
@Getter
@Setter
public class AddSubDto {

    @NotNull(message = "Choose Type!")
    private SubscriptionType type;
    @NotBlank(message = "Choose a name for the package!")
    private String namePackage;
    @NotNull(message = "Field cant be empty")
    @Positive(message = "Price must be a positive number!")
    private double price;

    @NotBlank
    private String description;
}
