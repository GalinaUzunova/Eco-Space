package org.ecospace.model.dto;



import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;


import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProfileDto {

    private UUID id;
    @Size(min = 6, max = 20, message = "Username must be between 6 and 20 characters!")
    private String username;
    @Email
    private String email;

    @URL
    private String imageURL;

}
