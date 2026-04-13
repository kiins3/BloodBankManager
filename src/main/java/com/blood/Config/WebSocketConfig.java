package com.blood.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Bật một broker nội bộ (in-memory).
        // Các client đăng ký theo dõi (subscribe) các đường dẫn bắt đầu bằng "/topic" sẽ nhận được tin nhắn.
        config.enableSimpleBroker("/topic");

        // Tiền tố cho các request từ Client gửi lên Server (nếu Client muốn gửi gì đó)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đây là cái "cổng làng" để Frontend kết nối vào.
        // VD: var socket = new SockJS('http://localhost:8080/ws-bloodbank');
        registry.addEndpoint("/ws-bloodbank")
                .setAllowedOriginPatterns("*") // Cho phép Frontend (React/Next) ở port khác kết nối vào
                .withSockJS(); // Fallback an toàn nếu trình duyệt cũ không hỗ trợ chuẩn WebSocket
    }
}
