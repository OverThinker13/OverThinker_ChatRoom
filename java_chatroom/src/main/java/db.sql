create database if not exists java_chatroom charset utf8;

use java_chatroom;

-- 创建用户表
drop table if exists user;
create table user (
      userId int primary key auto_increment,
      username varchar(20) unique,
      password varchar(20)
);

insert into user values(1, '张三', '123');
insert into user values(2, '李四', '123');
insert into user values(3, '王五', '123');
insert into user values(4, '赵六', '123');

-- 创建好友表
drop table if exists friend;
create table friend (
    userId int,
    friendId int
);

insert into friend values(1, 2);
insert into friend values(2, 1);
insert into friend values(1, 3);
insert into friend values(3, 1);
insert into friend values(1, 4);
insert into friend values(4, 1);

-- 创建会话表
drop table if exists  message_session;
create table message_session (
    sessionId int primary key auto_increment,
    -- s上次访问时间
    lastTime datetime
);

insert into message_session value (1,'2025-12-10 20:54:13');
insert into message_session value (2,'2005-10-13 20:54:13');


-- 创建会话和用户的关联表
drop table if exists message_session_user;
create table message_session_user(
  sessionId int,
  userId int
);

-- 1号会话里有张三和李四
insert into message_session_user values(1, 1), (1, 2);
-- 2号会话里有K和N
insert into message_session_user values(2, 5), (2, 6);


-- 创建消息表
drop table if exists message;
create  table  message(
    messageId int primary key auto_increment,
    fromId int, -- 消息是哪个用户发的
    sessionId int,  -- 消息属于哪个会话
    content varchar(2048), -- 消息正文
    postTime datetime -- 消息的发送时间
);

-- 构造几个消息数据，方便测试
-- 张三和李四发的消息
insert  into message values(1, 1, 1 ,'今晚吃啥', '2025-05-01 17:00:00');
insert  into message values(2, 2, 1 ,'随便', '2025-05-01 17:01:00');
insert  into message values(3, 1, 1 ,'那吃面？', '2025-05-01 17:02:00');
insert  into message values(4, 2, 1 ,'不想吃', '2025-05-01 17:03:00');
insert  into message values(5, 1, 1 ,'那你想吃啥', '2025-05-01 17:04:00');
insert  into message values(6, 2, 1 ,'随便', '2025-05-01 17:05:00');
-- K和N发的消息
insert  into message values(7, 5, 2 ,'呼叫N，Over', '2025-12-13 23:04:00');
insert  into message values(8, 6, 2 ,'N收到，Over', '2025-12-13 23:05:00');
