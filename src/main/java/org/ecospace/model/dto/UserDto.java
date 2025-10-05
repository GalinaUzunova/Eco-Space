package org.ecospace.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter@Setter
public class UserDto {



        @NotBlank(message = "Username is required!")
        @Size(min = 6, max = 20, message = "Username must be between 6 and 20 characters!")
        private String username;

        @Email(message = "Please provide a valid email address!")
        @NotBlank(message = "Email is required!")
        private String email;

        @NotBlank(message = "Password is required!")
        @Size(min = 6, message = "Password must be at least 6 characters!")
        private String password;

        @NotBlank
        private String confirmPassword;




        }




