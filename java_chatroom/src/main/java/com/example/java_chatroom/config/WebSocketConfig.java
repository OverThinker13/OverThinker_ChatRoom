package com.example.java_chatroom.config;

import com.example.java_chatroom.api.TestWebSocketAPI;
import com.example.java_chatroom.api.WebSocketAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private TestWebSocketAPI testWebSocketAPI;

    @Autowired
    private WebSocketAPI webSocketAPI;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 通过这个方法，把刚才创建好的Handler类注册到具体的路径上
        // 此时当这个浏览器，websocket的请求路径是”/test“的时候，就会调用到testWebSocketAPI这个方法
        registry.addHandler(testWebSocketAPI,"/test");  // 测试代码

        registry.addHandler(webSocketAPI, "/WebSocketMessage")
                // 通过注册这个特定的HttpSession拦截器，就可以把用户给HttpSession中添加的Attribute键值对
                // 往我们 WebSocketSession里也添加一份
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }
}
