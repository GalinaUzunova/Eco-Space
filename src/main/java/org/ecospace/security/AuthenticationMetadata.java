package org.ecospace.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.ecospace.model.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Collection;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Data
public class AuthenticationMetadata implements UserDetails {

    private String username;
    private String password;
    private UUID id;
    private UserRole role;
    private boolean isActive;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_" + role.name());
        return List.of(grantedAuthority);
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }


    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
