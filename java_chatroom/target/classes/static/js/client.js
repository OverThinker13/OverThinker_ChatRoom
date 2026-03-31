//////////////////////////////////////
// 这里实现标签页的切换
//////////////////////////////////////

function initSwitchTab() {
    // 1.先获取相关元素（标签页的按钮，会话列表，好友列表）
    let tabSession = document.querySelector('.tab .tab-session');
    let tabFriend = document.querySelector('.tab .tab-friend');
    // querySelectorAll可以同时选中所有元素，得到的结果是个数组
    // [0]为会话列表 [1]为好友列表
    let lists = document.querySelectorAll('.list');
    // 2. 针对标签页按钮，注册点击事件
    tabSession.onclick = function () {
        // a) 设置图标
        tabSession.style.backgroundImage = 'url(img/chat1.png)';
        tabFriend.style.backgroundImage = 'url(img/user2.png)';

        // b) 设置列表
        lists[0].classList = 'list';
        lists[1].classList = 'list hide';
    };
    tabFriend.onclick = function () {
        // a) 设置图标
        tabSession.style.backgroundImage = 'url(img/chat2.png)';
        tabFriend.style.backgroundImage = 'url(img/user1.png)';

        // b) 设置列表
        lists[0].classList = 'list hide';
        lists[1].classList = 'list';
    }
}

initSwitchTab();

//////////////////////////////////////
// 操作websocket
//////////////////////////////////////

//创建一个websocket实例
// let websocket = new WebSocket("ws://127.0.0.1:8080/WebSocketMessage");
let websocket = new WebSocket("ws://" + location.host + "/WebSocketMessage");

websocket.onopen = function () {
    console.log("websocket连接成功");
}

websocket.onmessage = function (e) {
    console.log("收到服务器端消息   ：" + e.data);
    let resp = JSON.parse(e.data);

    if (resp.type == 'message') {
        handleMessage(resp);
    } else {
        console.log('resp.type 不符合要求', resp);
    }
}

websocket.onerror = function () {
    console.log("websocket连接异常");
}

websocket.onclose = function () {
    console.log("websocket连接关闭");
    // 检查关闭原因，如果是正常登出则不需要提示
    // 如果是非正常关闭，可能是账号在其他地方登录
    alert("您的账号可能在其他地方登录，当前连接已被断开。");
    // 跳转到登录页面
    location.assign('/login.html');
}


function handleMessage(resp) {
    // 1.现根据响应中的sessionId，获取到当前会话的li标签
    let currentSessionLi = findSessionLi(resp.sessionId);

    // 未读消息: 如果会话 li 不存在，创建它
    if (currentSessionLi == null) {
        currentSessionLi = document.createElement('li');
        currentSessionLi.setAttribute('message-session-id', resp.sessionId);
        currentSessionLi.setAttribute('data-unread-count', 1);

        currentSessionLi.innerHTML = '<div class="session-info">'
            + '<h3>' + resp.fromName + '</h3>'
            + '<p></p>'
            + '</div>'
            + '<span class="unread-count">1</span>'; // 首次创建，显示 '1'

        currentSessionLi.onclick = function () {
            clickSession(currentSessionLi);
        }
    }

    // 2.把新的消息内容，展示到li标签的预览区域中
    let p = currentSessionLi.querySelector('p');
    p.innerHTML = resp.content;
    if (p.innerHTML.length > 10) {
        p.innerHTML = p.innerHTML.substring(0, 10) + '...';
    }

    // 3.把收到消息的会话，放在会话列表的顶部
    let sessionListUl = document.querySelector('#session-list');
    sessionListUl.insertBefore(currentSessionLi, sessionListUl.children[0]);

    // 4.判断是否是当前选中的会话
    if (currentSessionLi.className == 'selected') {
        // A) 当前会话选中：直接显示消息，并清除未读数
        let messageShowDiv = document.querySelector('.right .message-show');
        addMessage(messageShowDiv, resp);
        scrollBottom(messageShowDiv);

        // 清除未读数
        let unreadSpan = currentSessionLi.querySelector('.unread-count');
        if (unreadSpan) {
            unreadSpan.classList.add('hide');
        }
        currentSessionLi.setAttribute('data-unread-count', 0);

    } else {
        // B) 当前会话未选中：增加未读数并显示圆点
        let unreadSpan = currentSessionLi.querySelector('.unread-count');

        if (!unreadSpan) {
            // 如果是旧的 li 且没有 unreadSpan，则创建
            unreadSpan = document.createElement('span');
            unreadSpan.className = 'unread-count';
            currentSessionLi.appendChild(unreadSpan);
        }

        // 获取并更新未读数
        let currentCount = parseInt(currentSessionLi.getAttribute('data-unread-count') || 0) + 1;
        currentSessionLi.setAttribute('data-unread-count', currentCount);

        // 更新显示
        unreadSpan.innerText = currentCount;
        unreadSpan.classList.remove('hide'); // 确保显示圆点
    }
}

function findSessionLi(targetSessionId) {
    let sessionLis = document.querySelectorAll('#session-list li');
    for (let li of sessionLis) {
        let sessionId = li.getAttribute('message-session-id');
        if (sessionId == targetSessionId) {
            return li;
        }
    }
    return null;
}

//////////////////////////////////////
// 消息发送/接收逻辑
//////////////////////////////////////
function sendMessage() {
    let messageInput = document.querySelector('.right .message-input');

    if (!messageInput.value) {
        return;
    }

    let selectedLi = document.querySelector('#session-list .selected');
    if (selectedLi == null) {
        return;
    }
    let sessionId = selectedLi.getAttribute('message-session-id');

    let req = {
        type: 'message',
        sessionId: sessionId,
        content: messageInput.value,
    };
    req = JSON.stringify(req);
    console.log('要发送的消息：', req);

    websocket.send(req);
    messageInput.value = '';
}

function initSendMessage() {
    let sendButton = document.querySelector('.right .ctrl button');
    let messageInput = document.querySelector('.right .message-input');

    // 点击发送按钮发送消息
    sendButton.onclick = function () {
        sendMessage();
    };

    // 监听键盘事件实现回车发送，Shift+Enter换行
    messageInput.addEventListener('keydown', function (e) {
        // 兼容处理 e.key ('Enter') 和 e.keyCode (13)
        const isEnter = e.key === 'Enter' || e.keyCode === 13;

        if (isEnter && !e.shiftKey) {
            // 按Enter键发送消息
            e.preventDefault(); // <-- 关键：阻止默认的换行行为
            sendMessage();
        }
        // Shift + Enter (换行) 保持默认行为
    });
}
// ... 保持 initSendMessage(); 调用不变

initSendMessage();

//////////////////////////////////////
// 从服务器获取到用户登录数据
//////////////////////////////////////

function getUserInfo() {
    $.ajax({
        type: 'get',
        url: '/userInfo',
        success: function (body) {
            if (body.userId && body.userId > 0) {
                let userDiv = document.querySelector('.main .left .user');
                userDiv.innerHTML = body.username;
                userDiv.setAttribute("user-id", body.userId);
            } else {
                alert("当前用户未登录")
                location.assign('/login.html');
            }
        }
    })
}

getUserInfo()

//////////////////////////////////////
// 从浏览器中发送请求，获取用户列表
//////////////////////////////////////

function getFriendList() {
    $.ajax({
        type: 'get',
        url: "/friendList",
        success: function (body) {
            console.log('服务器返回的好友列表数据：', body);
            let friendListUL = document.querySelector('#friend-list');
            friendListUL.innerHTML = '';

            for (let friend of body) {
                let li = document.createElement('li');
                li.innerHTML = '<h4>' + friend.friendName + '</h4>';
                li.setAttribute('friend-id', friend.friendId);
                friendListUL.appendChild(li);

                li.onclick = function () {
                    clickFriend(friend);
                }
            }
        },
        error: function () {
            console.log('获取好友列表失败！');
        }
    })
}

getFriendList()

//////////////////////////////////////
// 从浏览器中发送请求，获取会话列表
//////////////////////////////////////

function getSessionList() {
    $.ajax({
        type: 'get',
        url: 'sessionList',
        dataType: 'json',
        success: function (body) {
            let sessionListUL = document.querySelector('#session-list');
            sessionListUL.innerHTML = '';

            for (let session of body) {
                if (session.lastMessage.length > 10) {
                    session.lastMessage = session.lastMessage.substring(0, 10) + '...';
                }

                let li = document.createElement('li');
                li.setAttribute('message-session-id', session.sessionId);

                // 【未读消息初始化】
                li.setAttribute('data-unread-count', 0);

                // 【修正】: 包裹 div.session-info 确保布局正确
                li.innerHTML = '<div class="session-info">'
                    + '<h3>' + session.friends[0].friendName + '</h3>'
                    + '<p>' + session.lastMessage + '</p>'
                    + '</div>'
                    + '<span class="unread-count hide"></span>'; // 默认隐藏

                sessionListUL.appendChild(li);

                li.onclick = function () {
                    clickSession(li);
                }
            }
        }
    });
}

getSessionList();

// ----------------------------------------------------
// clickSession: 清除未读数 (在点击会话时)
// ----------------------------------------------------
function clickSession(currentLi) {
    // 1.设置高亮
    let allLis = document.querySelectorAll("#session-list>li");
    activeSession(allLis, currentLi);

    // === 清除未读计数 ===
    let unreadSpan = currentLi.querySelector('.unread-count');
    if (unreadSpan) {
        unreadSpan.classList.add('hide'); // 隐藏圆圈
        unreadSpan.innerText = '';
    }
    // 重置存储的未读数
    currentLi.setAttribute('data-unread-count', 0);
    // ====================

    // 2，获取指定会话的历史消息
    let sessionId = currentLi.getAttribute("message-session-id");
    getHistoryMessage(sessionId);
}

function activeSession(allLis, currentLi) {
    for (let li of allLis) {
        if (li == currentLi) {
            li.className = 'selected';
        } else {
            li.className = '';
        }
    }
}

// 这个函数负责获取指定会话的历史消息
function getHistoryMessage(sessionId) {
    console.log("获取历史消息 sessionId=" + sessionId);
    // 1.先清空右侧列表的已有内容
    let titleDiv = document.querySelector('.right .title');
    titleDiv.innerHTML = '';
    let messageShowDiv = document.querySelector('.right .message-show');
    messageShowDiv.innerHTML = '';

    // 2.重新设置会话标题
    let selectedH3 = document.querySelector('#session-list .selected>h3');
    if (selectedH3) {
        titleDiv.innerHTML = selectedH3.innerHTML;
    }

    // 3.发送一个ajax请求给服务器，获取到绘画的历史消息
    $.ajax({
        type: 'get',
        url: 'message?sessionId=' + sessionId,
        dataType: 'json',
        success: function (body) {
            for (let message of body) {
                addMessage(messageShowDiv, message);
            }
            // 加个操作：在构造后消息列表之后，控制滚动条，自动滚动到最下方
            scrollBottom(messageShowDiv);
        }
    })
}

// 把指定元素滚动到最下方
function scrollBottom(elum) {
    if (!elum) {
        console.error("滚动元素为空!");
        return;
    }
    elum.scrollTop = elum.scrollHeight;
}

function addMessage(messageShowDiv, message) {
    let messageDiv = document.createElement('div');

    let selfUsername = document.querySelector('.left .user').innerHTML;
    if (selfUsername == message.fromName) {
        messageDiv.className = 'message message-right';
    } else {
        messageDiv.className = 'message message-left';
    }

    messageDiv.innerHTML = '<div class="box">'
        + '<div class="header">'
        + '<h4>' + message.fromName + '</h4>'
        + '<span class="timestamp">' + message.postTime + '</span>'
        + '</div>'
        + '<p>' + message.content + '</p>'
        + '</div>'
    messageShowDiv.appendChild(messageDiv);
}

// ----------------------------------------------------
// clickFriend: 初始化新会话结构 (从好友列表点击)
// ----------------------------------------------------
function clickFriend(friend) {
    let sessionLi = findSessionByName(friend.friendName);
    let sessionListUL = document.querySelector('#session-list');

    if (sessionLi) {
        sessionListUL.insertBefore(sessionLi, sessionListUL.children[0]);
        sessionLi.click();
    } else {
        // 3.如果不存在匹配的结果，就创建一个新会话
        sessionLi = document.createElement('li');

        // 【修正】: 包裹 div.session-info 确保布局正确 + 未读数初始化
        sessionLi.setAttribute('data-unread-count', 0);
        sessionLi.innerHTML = '<div class="session-info">'
            + '<h3>' + friend.friendName + '</h3>'
            + '<p></p>'
            + '</div>'
            + '<span class="unread-count hide"></span>';

        // 把标签进行置顶
        sessionListUL.insertBefore(sessionLi, sessionListUL.children[0]);
        sessionLi.onclick = function () {
            clickSession(sessionLi);
        }
        sessionLi.click();

        createSession(friend.friendId, sessionLi);
    }

    // 4.切换到会话列表
    let tabSession = document.querySelector('.tab .tab-session');
    tabSession.click();
}


function findSessionByName(username) {
    let sessionLis = document.querySelectorAll('#session-list>li');
    for (let sessionLi of sessionLis) {
        let h3 = sessionLi.querySelector('h3');
        if (h3.innerHTML == username) {
            return sessionLi;
        }
    }
    return null;
}

function createSession(friendId, sessionLi) {
    $.ajax({
        type: 'post',
        url: 'session?toUserId=' + friendId,
        success: function (body) {
            console.log("会话创建成功！ sessionId = " + body.sessionId);
            sessionLi.setAttribute('message-session-id', body.sessionId);
        },
        error: function () {
            console.log("会话创建失败！");
        }
    });
}