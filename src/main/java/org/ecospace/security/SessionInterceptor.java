package org.ecospace.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class SessionInterceptor implements HandlerInterceptor {

    public static final Set<String> UNAUTHENTICATED_POINTS =Set.of("/register", "/login", "/",  "/design", "/maintenance", "/contact");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(UNAUTHENTICATED_POINTS.contains(request.getServletPath())){

            return  true;
        }

        HttpSession session=request.getSession(false);
        if(session == null){
            response.sendRedirect("/");
            return false;

        }
        Object userId=session.getAttribute("userId");
        if(userId == null){
            session.invalidate();
            response.sendRedirect("/");
            return  false;

        }

        return true;

    }
}
