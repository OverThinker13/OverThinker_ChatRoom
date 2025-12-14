package com.example.java_chatroom.model;


// 使用一个Friend对象表示一个好友
public class Friend {
    private int friendId;
    private String friendName;

    public int getFriendId() {
        return friendId;
    }

    public void setFriendId(int firendId) {
        this.friendId = firendId;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }
}
