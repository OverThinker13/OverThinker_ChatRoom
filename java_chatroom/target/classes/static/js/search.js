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
        
        // 隐藏搜索结果列表
        document.querySelector('#search-result-list').classList.add('hide');
    };
    tabFriend.onclick = function () {
        // a) 设置图标
        tabSession.style.backgroundImage = 'url(img/chat2.png)';
        tabFriend.style.backgroundImage = 'url(img/user1.png)';

        // b) 设置列表
        lists[0].classList = 'list hide';
        lists[1].classList = 'list';
        
        // 隐藏搜索结果列表
        document.querySelector('#search-result-list').classList.add('hide');
    }
}

initSwitchTab();

//////////////////////////////////////
// 搜索功能
//////////////////////////////////////

function initSearch() {
    let searchInput = document.querySelector('#search-input');
    let searchButton = document.querySelector('#search-button');
    let searchResultList = document.querySelector('#search-result-list');

    // 搜索按钮点击事件
    searchButton.onclick = function () {
        let keyword = searchInput.value.trim();
        if (!keyword) {
            return;
        }

        // 发送搜索请求
        $.ajax({
            type: 'get',
            url: '/searchUser',
            data: { keyword: keyword },
            success: function (body) {
                // 清空搜索结果列表
                searchResultList.innerHTML = '';
                
                if (body.length === 0) {
                    let li = document.createElement('li');
                    li.innerHTML = '<h4>未找到用户</h4>';
                    searchResultList.appendChild(li);
                    searchResultList.classList.remove('hide');
                    return;
                }

                // 显示搜索结果
                for (let user of body) {
                    let li = document.createElement('li');
                    li.innerHTML = '<div class="search-user-info">' +
                        '<h4>' + user.username + '</h4>' +
                        '<button class="add-friend-btn" data-user-id="' + user.userId + '">添加好友</button>' +
                        '</div>';
                    searchResultList.appendChild(li);
                }

                // 显示搜索结果列表
                searchResultList.classList.remove('hide');

                // 为添加好友按钮注册点击事件
                let addFriendButtons = document.querySelectorAll('.add-friend-btn');
                for (let button of addFriendButtons) {
                    button.onclick = function () {
                        let friendId = parseInt(button.getAttribute('data-user-id'));
                        addFriend(friendId);
                    };
                }
            },
            error: function () {
                console.log('搜索用户失败！');
            }
        });
    };

    // 输入框回车搜索
    searchInput.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' || e.keyCode === 13) {
            searchButton.click();
        }
    });

    // 点击页面其他地方隐藏搜索结果
    document.addEventListener('click', function (e) {
        if (!searchInput.contains(e.target) && !searchResultList.contains(e.target)) {
            searchResultList.classList.add('hide');
        }
    });
}

function addFriend(friendId) {
    $.ajax({
        type: 'post',
        url: '/addFriend',
        data: { friendId: friendId },
        success: function (body) {
            if (body.success) {
                alert('添加好友成功！');
                // 刷新好友列表
                getFriendList();
                // 刷新会话列表
                getSessionList();
                // 隐藏搜索结果
                document.querySelector('#search-result-list').classList.add('hide');
                // 清空搜索框
                document.querySelector('#search-input').value = '';
            } else {
                alert('添加失败：' + body.message);
            }
        },
        error: function () {
            console.log('添加好友失败！');
            alert('添加好友失败，请稍后重试');
        }
    });
}

initSearch();
