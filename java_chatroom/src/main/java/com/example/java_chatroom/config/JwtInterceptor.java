package com.example.java_chatroom.config;

import com.example.java_chatroom.service.RedisTokenService;
import com.example.java_chatroom.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;

    public JwtInterceptor(JwtUtil jwtUtil, RedisTokenService redisTokenService) {
        this.jwtUtil = jwtUtil;
        this.redisTokenService = redisTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI();

        if (path.equals("/login") || path.equals("/register")
                || path.startsWith("/css/") || path.startsWith("/js/")
                || path.startsWith("/img/") || path.equals("/login.html")
                || path.equals("/register.html") || path.equals("/client.html")) {
            return true;
        }

        String token = extractTokenFromCookie(request);
        if (token == null) {
            request.setAttribute("userId", null);
            return true;
        }

        try {
            Claims claims = jwtUtil.parseToken(token);
            int userId = Integer.parseInt(claims.getSubject());
            String username = claims.get("username", String.class);

            if (redisTokenService.isValid(userId, token)) {
                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
            } else {
                request.setAttribute("userId", null);
            }
        } catch (Exception e) {
            request.setAttribute("userId", null);
        }
        return true;
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return null;
        for (Cookie cookie : cookies) {
            if ("token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
