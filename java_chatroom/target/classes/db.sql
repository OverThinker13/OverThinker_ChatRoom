-- 彻底删除旧库（谨慎操作，数据会全部消失）
drop database if exists java_chatroom;

-- 重新创建库
create database java_chatroom charset utf8mb4;
use java_chatroom;

-- 1. 用户表
create table user (
      userId int primary key auto_increment,
      username varchar(20) unique not null,
      password varchar(20) not null
);

-- 2. 好友表
create table friend (
        id int primary key auto_increment,
        userId int not null,
        friendId int not null,
        status tinyint default 1 comment '状态: 1-已添加, 0-待确认',
        createTime datetime default current_timestamp,
        foreign key (userId) references user(userId),
        foreign key (friendId) references user(userId),
        unique key unique_friend (userId, friendId)
);

-- 3. 好友请求表
create table friend_request (
                requestId int primary key auto_increment,
                fromUserId int not null,
                fromUserName varchar(50) not null,
                toUserId int not null,
                requestTime datetime default current_timestamp,
                status tinyint default 0 comment '状态: 0-待处理, 1-已同意, 2-已拒绝',
                handleTime datetime,
                foreign key (fromUserId) references user(userId),
                foreign key (toUserId) references user(userId),
                index idx_toUserId_status (toUserId, status)
);

-- 4. 会话表
create table message_session (
                 sessionId int primary key auto_increment,
                 lastTime datetime default current_timestamp
);

-- 5. 会话关联表
create table message_session_user(
                     sessionId int not null,
                     userId int not null,
                     foreign key (sessionId) references message_session(sessionId),
                     foreign key (userId) references user(userId),
                     primary key (sessionId, userId)
);

-- 6. 消息表
create table message(
        messageId int primary key auto_increment,
        fromId int not null,
        sessionId int not null,
        content varchar(2048),
        postTime datetime default current_timestamp,
        foreign key (fromId) references user(userId),
        foreign key (sessionId) references message_session(sessionId)
);