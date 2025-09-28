package org.ecospace.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Getter
@Setter
public class LoginDto {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
