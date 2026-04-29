package com.example.java_chatroom.config;

import com.example.java_chatroom.entity.User;
import com.example.java_chatroom.service.RedisTokenService;
import com.example.java_chatroom.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class JwtWebSocketInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;

    public JwtWebSocketInterceptor(JwtUtil jwtUtil, RedisTokenService redisTokenService) {
        this.jwtUtil = jwtUtil;
        this.redisTokenService = redisTokenService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = extractTokenFromCookie(servletRequest);
            if (token == null) {
                System.out.println("[JWT] WebSocket 缺少 token，拒绝连接");
                return false;
            }

            try {
                Claims claims = jwtUtil.parseToken(token);
                int userId = Integer.parseInt(claims.getSubject());
                String username = claims.get("username", String.class);

                if (!redisTokenService.isValid(userId, token)) {
                    System.out.println("[JWT] WebSocket token 已失效，拒绝连接");
                    return false;
                }

                User user = new User();
                user.setUserId(userId);
                user.setUsername(username);
                attributes.put("user", user);

                System.out.println("[JWT] WebSocket 认证成功: userId=" + userId + ", username=" + username);
                return true;
            } catch (Exception e) {
                System.out.println("[JWT] WebSocket token 无效: " + e.getMessage());
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
    }

    private String extractTokenFromCookie(ServletServerHttpRequest request) {
        jakarta.servlet.http.Cookie[] cookies = request.getServletRequest().getCookies();
        if (cookies == null)
            return null;
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            if ("token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
