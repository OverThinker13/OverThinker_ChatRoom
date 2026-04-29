package com.example.java_chatroom.controller;

import com.example.java_chatroom.component.OnlineUserManger;
import com.example.java_chatroom.entity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Component
public class WebSocketAPI extends TextWebSocketHandler {

    @Autowired
    private OnlineUserManger onlineUserManger;

    @Autowired
    private com.example.java_chatroom.mapper.MessageSessionMapper messageSessionMapper;

    @Autowired
    private com.example.java_chatroom.mapper.MessageMapper messageMapper;

    @Autowired
    private com.example.java_chatroom.mapper.FriendMapper friendMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("[WebSocketAPI] 连接成功！");
        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            System.out.println("[WebSocketAPI] 用户未登录，拒绝连接");
            session.close();
            return;
        }
        onlineUserManger.online(user.getUserId(), session);
        notifyFriends(user, true);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("[WebSocketAPI] 收到消息！" + message);
        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            System.out.println("[WebSocketAPI] user == null! 未登录用户，无法进行消息转发");
            return;
        }
        MessageRequest req = objectMapper.readValue(message.getPayload(), MessageRequest.class);
        if (req.getType().equals("message")) {
            transferMessage(user, req);
        } else {
            System.out.println("[WebSocketAPI] req.type有误" + message.getPayload());
        }
    }

    private void transferMessage(User fromUser, MessageRequest req) throws IOException {
        MessageResponse resp = new MessageResponse();
        resp.setType("message");
        resp.setFromId(fromUser.getUserId());
        resp.setFromName(fromUser.getUsername());
        resp.setSessionId(req.getSessionId());
        resp.setContent(req.getContent());
        resp.setPostTime(new Date());
        String respJson = objectMapper.writeValueAsString(resp);

        List<Friend> friends = messageSessionMapper.getFriendsIdBySessionId(req.getSessionId(), fromUser.getUserId());
        Friend myself = new Friend();
        myself.setFriendId(fromUser.getUserId());
        myself.setFriendName(fromUser.getUsername());
        friends.add(myself);

        for (Friend friend : friends) {
            WebSocketSession webSocketSession = onlineUserManger.getSession(friend.getFriendId());
            if (webSocketSession == null) {
                continue;
            }
            webSocketSession.sendMessage(new TextMessage(respJson));
        }

        Message message = new Message();
        message.setFromId(fromUser.getUserId());
        message.setSessionId(req.getSessionId());
        message.setContent(req.getContent());
        messageMapper.add(message);
    }

    private void notifyFriends(User user, boolean online) throws IOException {
        List<Friend> friends = friendMapper.selectFriendList(user.getUserId());

        MessageResponse resp = new MessageResponse();
        resp.setType(online ? "online" : "offline");
        resp.setFromId(user.getUserId());
        resp.setFromName(user.getUsername());

        String respJson = objectMapper.writeValueAsString(resp);

        for (Friend friend : friends) {
            WebSocketSession friendSession = onlineUserManger.getSession(friend.getFriendId());
            if (friendSession != null) {
                friendSession.sendMessage(new TextMessage(respJson));
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.out.println("[WebSocketAPI] 连接异常！" + exception);
        User user = (User) session.getAttributes().get("user");
        if (user == null) return;
        onlineUserManger.offline(user.getUserId(), session);
        try {
            notifyFriends(user, false);
        } catch (IOException e) {
            System.out.println("[WebSocketAPI] 通知好友下线失败: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("[WebSocketAPI] 连接断开！" + status);
        User user = (User) session.getAttributes().get("user");
        if (user == null) return;
        onlineUserManger.offline(user.getUserId(), session);
        notifyFriends(user, false);
    }
}
