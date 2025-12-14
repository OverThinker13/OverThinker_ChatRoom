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
    // 如果是点击 会话标签按钮，就会把会话标签按钮的背景图片进行设置，
    // 同时把会话列表显示出来，，把好友列表隐藏；相反同理
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
let websocket = new WebSocket("ws://127.0.0.1:8080/WebSocketMessage");

websocket.onopen = function () {
    // 连接成功时，就会自动执行
    console.log("websocket连接成功");
}

websocket.onmessage = function (e) {
    // 当服务器端发送消息到客户端时，就会自动执行
    console.log("收到服务器端消息：" + e.data);
    // 此时收到的e.data是一个json字符串，我们需要把它解析成一个js对象
    let resp = JSON.parse(e.data);
    // 然后根据resp.type来判断是哪种消息
    if (resp.type == 'message') {
        // 说明是聊天消息,处理消息响应
        handleMessage(resp);
    } else {
        // 说明是resp的type出错
        console.log('resp.type 不符合要求', resp);
    }
}

websocket.onerror = function () {
    // 当连接发生异常时，就会自动执行
    console.log("websocket连接异常");
}

websocket.onclose = function () {
    // 当连接关闭时，就会自动执行
    console.log("websocket连接关闭");
}

function handleMessage(resp) {
    // 把客户端收到的消息，给展示出来，
    // 展示到对应的会话预览区域以及右侧消息列表中

    // 1.现根据响应中的sessionId，获取到当前会话的li标签
    // 如果li标签不存在则创建一个新的li标签
    let currentSessionLi = findSessionLi(resp.sessionId);
    if (currentSessionLi == null) {
        // 说明当前会话的li标签不存在，需要创建一个新的li标签，表示一个新的会话
        currentSessionLi = document.createElement('li');
        currentSessionLi.setAttribute('message-session-id', resp.sessionId);
        // 此处的p标签应该放会话预览区域，一会后面统一完成，先置空
        currentSessionLi.innerHTML = '<h3>' + resp.fromName + '</h3>'
            + '<p></p>';
        // 给li标签也加上点击事件的处理
        currentSessionLi.onclick = function () {
            clickSession(li);
        }
    }
    // 2.把新的消息内容，展示到li标签的预览区域中
    // 消息太长需要进行截断
    let p = currentSessionLi.querySelector('p');
    p.innerHTML = resp.content;
    if (p.innerHTML.length > 10) {
        p.innerHTML = p.innerHTML.substring(0, 10) + '...';
    }
    // 3.把收到消息的会话，放在会话列表的顶部
    let sessionListUl = document.querySelector('#session-list');
    sessionListUl.insertBefore(currentSessionLi, sessionListUl.children[0]);
    // 4.如果当前会话是被选中的会话，则把当前消息放在右侧消息列表中
    // 新增消息的同时，注意调整滚动条的位置，保证新消息虽然放在底部，但是能够被用户直接看到
    if (currentSessionLi.className == 'selected') {
        // 把消息列表添加一个新消息
        let messageShowDiv = document.querySelector('.right .message-show');
        addMessage(messageShowDiv, resp);
        // 新增消息后，需要调整滚动条的位置，保证新消息能够被用户直接看到
        scrollBottom(messageShowDiv);
    }
    // 其他操作，还可以在会话窗口给个提示（有新消息未读），还可以播放一个提示音
    // 这些操作是纯前端的，暂时不做
}

function findSessionLi(targetSessionId) {
    // 获取到所有的会话列表的li标签
    let sessionLis = document.querySelectorAll('#session-list li');
    for (let li of sessionLis) {
        // 针对每个li标签，获取到它的message-session-id属性值
        let sessionId = li.getAttribute('message-session-id');
        if (sessionId == targetSessionId) {
            // 如果属性值和参数中的sessionId相等，则说明是要找的li标签
            return li;
        }
    }
    // 这里啥时候会触发这个操作，就比如当前新的用户直接给当前用户发送消息，此时没存在现成的会话li标签
    return null;
}

//////////////////////////////////////
// 消息发送/接收逻辑
//////////////////////////////////////
function initSendButton() {

    // 1.获取到发送按钮和消息输入框
    let sendButton = document.querySelector('.right .ctrl button');
    let messageInput = document.querySelector('.right .message-input');
    // 2.给发送按钮注册一个点击事件
    sendButton.onclick = function () {
        // a) 现针对输入框的内容做个简单判定，比如输入框内容为空，则啥都不干
        if (!messageInput.value) {
            // value的值是null或者‘’都会触发这个条件
            return;
        }
        // b) 获取当前选中的li标签的sessionId
        let selectedLi = document.querySelector('#session-list .selected');
        if (selectedLi == null) {
            // 如果没有选中的li标签，则啥都不干
            return;
        }
        let sessionId = selectedLi.getAttribute('message-session-id');
        // c) 构造json数据
        let req = {
            type: 'message',
            sessionId: sessionId,
            content: messageInput.value,
        };
        req = JSON.stringify(req);
        console.log('要发送的消息：', req);
        // d) 通过websocket发送消息
        websocket.send(req);
        // e) 清空输入框的内容
        messageInput.value = '';
    }
}

initSendButton();
//////////////////////////////////////
// 从服务器获取到用户登录数据
//////////////////////////////////////

function getUserInfo() {
    $.ajax({
        type: 'get',
        url: '/userInfo',
        success: function (body) {
            // 从服务器获取到的数据
            // 校验结果是否有效
            if (body.userId && body.userId > 0) {
                // 如果结果有效，把用户名显示在页面上
                let userDiv = document.querySelector('.main .left .user');
                userDiv.innerHTML = body.username;
                userDiv.setAttribute("user-id", body.userId);

            } else {
                // 如果结果无效，则跳转到登录页页面，
                alert("当前用户未登录")
                location.assign('/login.html');
            }

            // 同时也可以记录userid到html标签的属性中（以备后用）
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
            // 1.先把之前的好友列表的内容给清空
            let friendListUL = document.querySelector('#friend-list');
            friendListUL.innerHTML = '';
            // 2.遍历body把服务器响应的结果，加回到friend-list中
            for (let friend of body) {
                let li = document.createElement('li');
                li.innerHTML = '<h4>' + friend.friendName + '</h4>';
                // 此处把friendId也记录下来，以备后用
                // 把friendId作为一个html的自定义属性加到li标签上
                li.setAttribute('friend-id', friend.friendId);
                friendListUL.appendChild(li);

                // 每个li标签，就对应界面上一个好友的选项，给这个li加上点击事件的处理
                li.onclick = function () {
                    // 参数表示区分了当前用户点击的是哪个好友
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
            // 1. 清空之前的会话列表
            let sessionListUL = document.querySelector('#session-list');
            sessionListUL.innerHTML = '';
            // 2. 遍历响应的数组, 针对结果来构造页面
            for (let session of body) {
                // 针对 lastMessage 的长度进行截断处理
                if (session.lastMessage.length > 10) {
                    session.lastMessage = session.lastMessage.substring(0, 10) + '...';
                }

                let li = document.createElement('li');
                // 把会话 id 保存到 li 标签的自定义属性中
                li.setAttribute('message-session-id', session.sessionId);
                li.innerHTML = '<h3>' + session.friends[0].friendName + '</h3>'
                    + '<p>' + session.lastMessage + '</p>';
                sessionListUL.appendChild(li);

                // 给 li 标签新增点击事件
                li.onclick = function () {
                    // 这个写法, 就能保证, 点击哪个 li 标签
                    // 此处对应的 clickSession 函数的参数就能拿到哪个 li 标签. 
                    clickSession(li);
                }
            }
        }
    });
}

getSessionList();

function clickSession(currentLi) {
    // 1.设置高亮
    let allLis = document.querySelectorAll("#session-list>li");
    activeSession(allLis, currentLi);
    // 2，获取指定会话的历史消息 TODO
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
    // 2.重新设置会话标题，新的会话标题就是点击的好友的名字
    // 先找到当前选中的会话是哪个，被选中的会话是带有selected类名的li标签
    let selectedH3 = document.querySelector('#session-list .selected>h3');
    if (selectedH3) {
        // selectedH3 可能不存在，比如页面加载阶段，并没有选中任何会话被选中
        // 也就没有会话带有selected标签，此时就无法查询到选中 selectedH3 标签
        titleDiv.innerHTML = selectedH3.innerHTML;
    }
    // 3.发送一个ajax请求给服务器，获取到绘画的历史消息
    $.ajax({
        type: 'get',
        url: 'message?sessionId=' + sessionId,
        dataType: 'json',
        success: function (body) {
            // 此处返回的body是个js对象数组，里面的每个元素都是一条消息
            // 直接遍历即可
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
    // 确保 elum 存在
    if (!elum) {
        console.error("滚动元素为空!");
        return;
    }
    // 只需要将元素的 scrollTop 属性设置为其内容的完整高度。
    // 这将强制滚动条位于最底部。
    elum.scrollTop = elum.scrollHeight;
}

function addMessage(messageShowDiv, message) {
    //使用这个div表示一条消息
    let messageDiv = document.createElement('div');
    // 此处需要针对当前消息是否为用户自己发的消息决定靠左还是靠右
    let selfUsername = document.querySelector('.left .user').innerHTML;
    if (selfUsername == message.fromName) {
        // 消息是自己发的，靠右
        messageDiv.className = 'message message-right';
    } else {
        // 消息是别人发的，靠左
        messageDiv.className = 'message message-left';

    }
    messageDiv.innerHTML = '<div class="box">'
        + '<div class="header">'
        + '<h4>' + message.fromName + '</h4>'
        + '<span class="timestamp">' + message.postTime + '</span>'
        + '</div>'
        + '<p>' + message.content + '</p>'
        + '</div>'
    // 最后把这个div添加到右侧消息展示区域
    messageShowDiv.appendChild(messageDiv);
}

// 点击好友列表项，触发的函数
function clickFriend(friend) {
    // 1.先判定下当前这个好友是否有对应的对话
    let sessionLi = findSessionByName(friend.friendName);
    let sessionListUL = document.querySelector('#session-list');
    if (sessionLi) {
        // 2.如果存在匹配的结果，就把这个会话设置为选中状态，并且置顶
        // insertBefore把找到的li标签放到最前面去
        sessionListUL.insertBefore(sessionLi, sessionListUL.children[0]);
        // 此处设置会话选中状态，获取历史消息已经存在，调用即可
        // clickSession(sessionLi);
        // 或者模拟点击一下操作
        sessionLi.click();
    } else {
        // 3.如果不存在匹配的结果，就创建一个新会话（创建li标签+通知服务器）
        sessionLi = document.createElement('li');
        // 构造具体的li标签内容，由于新会话没有最后一条消息，依次标签设置为空即可
        sessionLi.innerHTML = '<h3>' + friend.friendName + '</h3>' + '<p></p>';
        // 把标签进行置顶
        sessionListUL.insertBefore(sessionLi, sessionListUL.children[0]);
        sessionLi.onclick = function () {
            clickSession(sessionLi);
        }
        sessionLi.click();
        // 发送消息给服务器，告诉服务器当前新创建的会话是啥样的
        createSession(friend.friendId, sessionLi);
    }
    // 4.还需要把标签页给切换到 会话列表
    // 实现方式很容易，只要找到会话列表标签页按钮，模拟一个点击操作
    let tabSession = document.querySelector('.tab .tab-session');
    tabSession.click();
}


function findSessionByName(username) {
    // 先获取会话列表中所有的li标签
    // 然后依次遍历，看看这些li标签谁的名字和查找的名字一致
    let sessionLis = document.querySelectorAll('#session-list>li');
    for (let sessionLi of sessionLis) {
        // 获取到该li标签的h3标签，进一步得到名字
        let h3 = sessionLi.querySelector('h3');
        if (h3.innerHTML == username) {
            return sessionLi;
        }
    }
    return null;
}

// friend是构造http请求时必备的信息
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