package com.example.java_chatroom.model;

public class User {
    private int userId;  // 成员变量：userId（正确）
    private String username = "";
    private String password = "";

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    // 其他方法保持不变
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +  // 注意这里也修正为 userId
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
