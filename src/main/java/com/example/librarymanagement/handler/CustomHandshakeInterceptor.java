package com.example.librarymanagement.handler;

import com.example.librarymanagement.security.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtTokenProvider jwtTokenProvider;

    // Spring sẽ tự động gọi hàm beforeHandshake() trước khi kết nối WebSocket được chấp nhận
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String token = servletRequest.getServletRequest().getParameter("token");

            if (token != null && jwtTokenProvider.validateAccessToken(token)) {
                Integer userId = Integer.parseInt(jwtTokenProvider.extractSubject(token,
                        JwtTokenProvider.TokenKind.ACCESS));
                attributes.put("userId", userId); // Gắn userId vào attributes của session WebSocke
                log.info("WebSocket handshake successful for userId: {}", userId);
                return true; // Cho phép handshake, tức là WebSocket connection được mở
            }
        }
        log.warn("WebSocket handshake failed - invalid or missing token");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
