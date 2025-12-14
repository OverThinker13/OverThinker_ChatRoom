package com.example.java_chatroom.api;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class TestWebSocketAPI extends TextWebSocketHandler{
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 这个方法会在websocket链接建立成功后自动调用
        System.out.println("TsetAPI 连接成功");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //这个方法是在websocket收到信息的时候自动调用
        System.out.println("TsetAPI 收到消息" + message.toString());
        // session诗歌绘画，里面就记录了通信双方是谁，（session中就持有了websocket的通信连接）
        session.sendMessage(message);

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        //这个方法是在websocket连接出现异常的时候自动调用
        System.out.println("TsetAPI 连接异常");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        //这个方法是在websocket连接正常关闭后的时候自动调用
        System.out.println("TsetAPI 连接关闭");
    }
}
