package com.example.java_chatroom.api;

import com.example.java_chatroom.model.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
public class MessageSessionAPI {
    @Resource
    private MessageSessionMapper messageSessionMapper;

    @Resource
    private MessageMapper messageMapper;

    @GetMapping("/sessionList")
    @ResponseBody
    public Object getMessageSessionList(HttpServletRequest req) {

        List<MessageSession> messageSessionList = new ArrayList<>();

        // 1.获取到当前用户的userId（从spring的session获取）
        HttpSession session = req.getSession(false);
        if(session == null) {
            System.out.println("[getMessageSessionList] session == null");
            return messageSessionList;
        }

        User user = (User) session.getAttribute("user");
        if(user == null) {
            System.out.println("[getMessageSessionList] user == null");
            return messageSessionList;
        }
        // 2.根据userId查询数据库，查出来有哪些会话id
        List<Integer> sessionIdList =  messageSessionMapper.getSessionIdByUserId(user.getUserId());
        for(int sessionId : sessionIdList) {
            MessageSession messageSession = new MessageSession();
            messageSession.setSessionId(sessionId);
            // 3.遍历会话id，查询出每个会话里涉及到的好友都有谁
            List<Friend> friends = messageSessionMapper.getFriendsIdBySessionId(sessionId, user.getUserId());
            messageSession.setFriends(friends);
            // 4.遍历会话id，查询出每个会话的最后一条消息
            String lastMessage = messageMapper.getLastMessageBySessionId(sessionId);

            // 有可能按照会话id查不到的情况，毕竟新创建的会话可能还没来得及发消息
            if(lastMessage == null) {
                messageSession.setLastMessage("");
            }else {
                messageSession.setLastMessage(lastMessage);
            }
            messageSessionList.add(messageSession);
        }
        // 最终目标就是构造出一个MessageSession对象数组
        return messageSessionList;
    }

    @PostMapping("/session")
    @ResponseBody
    // 通过注解获取user对象
    public Object addMessageSession(int toUserId, @SessionAttribute("user") User user) {

        HashMap<String, Integer> resp = new HashMap<>();

        // 进行数据库的插入操作
        // 1.先给message_session表里插入记录,是哦也能够这个参数的目的主要是为了能获取到会话的sessionId
        // 换而言之。MessageSession里的friends和lastMessage属性在此处都用不上
        MessageSession messageSession = new MessageSession();
        messageSessionMapper.addMessageSession(messageSession);
        // 2.给message_session_user 表插入记录
        MessageSessionUserItem item1 = new MessageSessionUserItem();
        item1.setSessionId(messageSession.getSessionId());
        item1.setUserId(user.getUserId());
        messageSessionMapper.addMessageSessionUser(item1);
        // 3.给message_session_user表插入记录
        MessageSessionUserItem item2 = new MessageSessionUserItem();
        item2.setSessionId(messageSession.getSessionId());
        item2.setUserId(toUserId);
        messageSessionMapper.addMessageSessionUser(item2);
        resp.put("sessionId", messageSession.getSessionId());

        System.out.println("[addMessageSession] 新增会话成功 sessionId: " + messageSession.getSessionId()
                + ", userId1: " + user.getUserId() + ", userId2: " + toUserId);

        // 返回的对象是一个普通对象也可以，或者是一个Map也可以，jackson都能处理
        return resp;
    }
}
