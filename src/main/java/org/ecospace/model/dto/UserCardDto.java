package org.ecospace.model.dto;


import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class UserCardDto {
    @NotBlank
    @Pattern(regexp = "^\\d+$", message = "Field must contain only digits")

    private String card;
    @NotBlank
    private String name;
    @NotBlank
    private String expiresOn;
    @NotBlank
    private String cvv;


}
