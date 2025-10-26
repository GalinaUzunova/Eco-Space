package org.ecospace.init;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String redirectUrl = determineTargetUrl(authorities);

        System.out.println("Authentication successful. Redirecting to: " + redirectUrl);
        System.out.println("User authorities: " + authorities);
        response.sendRedirect(request.getContextPath() + redirectUrl);

    }

    private String determineTargetUrl(Collection<? extends GrantedAuthority> authorities) {
        for (GrantedAuthority authority : authorities) {

            if (authority.getAuthority().equals("ROLE_ADMIN")){

                return "/manager";

            }  else if (authority.getAuthority().equals("ROLE_CLIENT")) {
                return "/client";
            }
        }
        return "/";
    }

}
