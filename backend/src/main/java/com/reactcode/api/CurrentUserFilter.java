package com.reactcode.api;

import com.reactcode.domain.AppUser;
import com.reactcode.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CurrentUserFilter extends OncePerRequestFilter {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ATTR = "reactcode.user";

    private final UserService userService;

    public CurrentUserFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/")) {
            AppUser user = userService.resolveOrCreate(request.getHeader(USER_ID_HEADER));
            request.setAttribute(USER_ATTR, user);
            response.setHeader(USER_ID_HEADER, user.getId().toString());
        }
        filterChain.doFilter(request, response);
    }
}
