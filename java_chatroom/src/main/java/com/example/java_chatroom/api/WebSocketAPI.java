package com.example.java_chatroom.api;

import com.example.java_chatroom.component.OnlineUserManger;
import com.example.java_chatroom.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
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
    private MessageSessionMapper messageSessionMapper;

    @Autowired
    private MessageMapper messageMapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("[WebSocketAPI] 连接成功！");
        User user = (User)session.getAttributes().get("user");
        if (user == null) {
            System.out.println("[WebSocketAPI] 用户未登录，拒绝连接");
            session.close();
            return;
        }
        // 把这个键值对存起来
        onlineUserManger.online(user.getUserId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("[WebSocketAPI] 收到消息！" + message.toString());
        // 处理消息的接受，转发，以及消息的保存记录
        // 1.获取到当前用户的信息，后续进行消息转发啥的
        User user = (User)session.getAttributes().get("user");
        if (user == null) {
            System.out.println("[WebSocketAPI] user == null! 未登录用户，无法进行消息转发");
            return;
        }
        // 2.针对请求进行解析,把json格式的字符串
        MessageRequest req = objectMapper.readValue(message.getPayload(), MessageRequest.class);
        if(req.getType().equals("message")) {
            // 进行消息转发
            transferMessage(user,req);
        }else {
            System.out.println("[WebSocketAPI] req.type有误" + message.getPayload());
        }

    }


    // 通过这个方法来完成消息实际的转发工作
    // 第一个参数表示要转发的消息是从谁来的
    private void transferMessage(User fromUser, MessageRequest req) throws IOException {
        // 1.先构造一个待转发的响应对象 MessageResponse
        MessageResponse resp = new MessageResponse();
        resp.setType("message");
        resp.setFromId(fromUser.getUserId());
        resp.setFromName(fromUser.getUsername());
        resp.setSessionId(req.getSessionId());
        resp.setContent(req.getContent());
        resp.setPostTime(new Date());
        // 把这个java对象转成json字符串
        String respJson = objectMapper.writeValueAsString(resp);
        System.out.println("[transferMessage] respJson: " + respJson);

        // 2.根据请求中的sessionId，来获取到这个MessageSession里都有那些用户，通过查询数据库可以知道
        List<Friend> friends = messageSessionMapper.getFriendsIdBySessionId(req.getSessionId(), fromUser.getUserId());
        // 此处注意！！！上述数据库查询，会把当前发消息的用户排除掉，而我们最终转发时，则需要也把发送消息的也发一次
        // 把当前用户也添加到List中
        Friend myself = new Friend();
        myself.setFriendId(fromUser.getUserId());
        myself.setFriendName(fromUser.getUsername());
        friends.add(myself);

        // 3.循环遍历上诉列表，给列表中的每个元素都发一个响应消息
        // 注意：除了给查询到的好友发，也要给自己发一个，方便我们实现在自己的客户端上显示自己发送的消息
        // 注意：一个会话中，可能有多个用户（群聊），虽然客户端没有支持群聊的（前端写起来相对麻烦）但是无论是后端API还是
        //     数据库都是支持群聊的，此处的转发逻辑一样也让他支持群聊
        for (Friend friend : friends) {
            // 知道了每个用户的userId，进一步的查询刚才准备好的OnLineUserManger，就知道对应的WebSocketSession
            // 从而进行发送消息
            WebSocketSession webSocketSession = onlineUserManger.getSession(friend.getFriendId());
            if(webSocketSession == null) {
                // 如果该用户未在线，则不发送
                continue;
            }

            webSocketSession.sendMessage(new TextMessage(respJson));
        }

        // 4.转发的消息还需要放到数据库，后续用户如果用户下线之后，重新上线，还可以通过历史消息的方式拿到之前的消息
        // 需要往message表中写入一条数据
        Message message = new Message();
        message.setFromId(fromUser.getUserId());
        message.setSessionId(req.getSessionId());
        message.setContent(req.getContent());
        // 像自增主键，还有时间这样的属性都可以让SQL在数据库中自动生成
        messageMapper.add(message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("[WebSocketAPI] 连接异常！" + exception.toString());

        User user = (User)session.getAttributes().get("user");
        if (user == null) {
            return;
        }
        onlineUserManger.offline(user.getUserId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("[WebSocketAPI] 连接断开！" + status.toString());

        User user = (User)session.getAttributes().get("user");
        if (user == null) {
            return;
        }
        onlineUserManger.offline(user.getUserId(), session);
    }
}
