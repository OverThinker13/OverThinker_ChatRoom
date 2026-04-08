package com.example.java_chatroom.service.impl;

import com.example.java_chatroom.entity.Friend;
import com.example.java_chatroom.entity.MessageSession;
import com.example.java_chatroom.entity.MessageSessionUserItem;
import com.example.java_chatroom.mapper.MessageMapper;
import com.example.java_chatroom.mapper.MessageSessionMapper;
import com.example.java_chatroom.service.MessageSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MessageSessionServiceImpl implements MessageSessionService {

    @Resource
    private MessageSessionMapper messageSessionMapper;

    @Resource
    private MessageMapper messageMapper;

    @Override
    public List<MessageSession> getSessionList(int userId) {
        List<MessageSession> result = new ArrayList<>();
        List<Integer> sessionIdList = messageSessionMapper.getSessionIdByUserId(userId);
        for (int sessionId : sessionIdList) {
            MessageSession messageSession = new MessageSession();
            messageSession.setSessionId(sessionId);
            List<Friend> friends = messageSessionMapper.getFriendsIdBySessionId(sessionId, userId);
            messageSession.setFriends(friends);
            String lastMessage = messageMapper.getLastMessageBySessionId(sessionId);
            messageSession.setLastMessage(lastMessage == null ? "" : lastMessage);
            result.add(messageSession);
        }
        return result;
    }

    @Override
    public int createSession(int userId, int toUserId) {
        MessageSession messageSession = new MessageSession();
        messageSessionMapper.addMessageSession(messageSession);

        MessageSessionUserItem item1 = new MessageSessionUserItem();
        item1.setSessionId(messageSession.getSessionId());
        item1.setUserId(userId);
        messageSessionMapper.addMessageSessionUser(item1);

        MessageSessionUserItem item2 = new MessageSessionUserItem();
        item2.setSessionId(messageSession.getSessionId());
        item2.setUserId(toUserId);
        messageSessionMapper.addMessageSessionUser(item2);

        log.info("[createSession] sessionId={}, userId1={}, userId2={}", messageSession.getSessionId(), userId, toUserId);
        return messageSession.getSessionId();
    }
}
