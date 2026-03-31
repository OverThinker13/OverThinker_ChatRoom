    create database if not exists java_chatroom charset utf8mb4; -- 建议用 utf8mb4 兼容表情包
use java_chatroom;

-- 1. 用户表
drop table if exists user;
create table user (
    userId int primary key auto_increment,
    username varchar(20) unique not null,
    password varchar(20) not null
);

-- 插入测试数据
insert into user values(1, '张三', '123');
insert into user values(2, '李四', '123');
insert into user values(3, '王五', '123');
insert into user values(4, '赵六', '123');
insert into user values(5, 'K', 'Azxc12388');
insert into user values(6, 'N', 'Azxc12388');
insert into user values(7, 'Thinker', '12208');

-- 2. 好友表（存储最终的关系）
drop table if exists friend;
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

-- 插入好友关系数据
insert into friend values(1, 1, 2, 1, '2025-05-01 17:00:00');
insert into friend values(2, 2, 1, 1, '2025-05-01 17:00:00');
insert into friend values(3, 1, 3, 1, '2025-05-01 17:00:00');
insert into friend values(4, 3, 1, 1, '2025-05-01 17:00:00');
insert into friend values(5, 1, 4, 1, '2025-05-01 17:00:00');
insert into friend values(6, 4, 1, 1, '2025-05-01 17:00:00');
insert into friend values(7, 5, 6, 1, '2025-12-13 23:04:00');
insert into friend values(8, 6, 5, 1, '2025-12-13 23:04:00');
insert into friend values(9, 5, 7, 1, '2025-12-13 23:04:00');
insert into friend values(10, 7, 5, 1, '2025-12-13 23:04:00');

-- 3. 好友请求表
drop table if exists friend_request;
create table friend_request (
    requestId int primary key auto_increment,
    fromUserId int not null,
    toUserId int not null,
    requestTime datetime default current_timestamp,
    status tinyint default 0 comment '状态: 0-待处理, 1-已同意, 2-已拒绝',
    handleTime datetime,
    foreign key (fromUserId) references user(userId),
    foreign key (toUserId) references user(userId),
    index idx_toUserId_status (toUserId, status)
);

-- 4. 会话表
drop table if exists message_session;
create table message_session (
    sessionId int primary key auto_increment,
    lastTime datetime default current_timestamp
);

-- 插入会话数据
insert into message_session value (1,'2025-12-10 20:54:13');
insert into message_session value (2,'2005-10-13 20:54:13');

-- 5. 会话关联表
drop table if exists message_session_user;
create table message_session_user(
    sessionId int not null,
    userId int not null,
    foreign key (sessionId) references message_session(sessionId),
    foreign key (userId) references user(userId),
    primary key (sessionId, userId)
);

-- 插入会话关联数据
-- 1号会话里有张三和李四
insert into message_session_user values(1, 1), (1, 2);
-- 2号会话里有K和N
insert into message_session_user values(2, 5), (2, 6);

-- 6. 消息表
drop table if exists message;
create table message(
    messageId int primary key auto_increment,
    fromId int not null,
    sessionId int not null,
    content varchar(2048),
    postTime datetime default current_timestamp,
    foreign key (fromId) references user(userId),
    foreign key (sessionId) references message_session(sessionId)
);

-- 插入消息数据
-- 张三和李四发的消息
insert into message values(1, 1, 1 ,'今晚吃啥', '2025-05-01 17:00:00');
insert into message values(2, 2, 1 ,'随便', '2025-05-01 17:01:00');
insert into message values(3, 1, 1 ,'那吃面？', '2025-05-01 17:02:00');
insert into message values(4, 2, 1 ,'不想吃', '2025-05-01 17:03:00');
insert into message values(5, 1, 1 ,'那你想吃啥', '2025-05-01 17:04:00');
insert into message values(6, 2, 1 ,'随便', '2025-05-01 17:05:00');
-- K和N发的消息
insert into message values(7, 5, 2 ,'呼叫N，Over', '2025-12-13 23:04:00');
insert into message values(8, 6, 2 ,'N收到，Over', '2025-12-13 23:05:00');
