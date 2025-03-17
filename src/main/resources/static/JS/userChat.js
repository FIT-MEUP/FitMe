// userChat.js
$(document).ready(function(){
  // 1. 페이지 로드시 초기 unread 메시지 개수를 hidden 요소에서 읽어 배지에 반영
  var initialUnreadCount = parseInt($("#trainerUnreadCount").text()) || 0;
  if (initialUnreadCount > 0) {
    $("#trainerUnreadBadge").text(initialUnreadCount).show();
  } else {
    $("#trainerUnreadBadge").hide();
  }

  // 2. 알림용 WebSocket 연결 (채팅창이 열리지 않은 상태에서도 unread 업데이트)
  function connectNotificationWebSocket(){
    var socket = new SockJS('/ws');
    var notificationClient = Stomp.over(socket);
    notificationClient.connect({}, function(frame){
      console.log("알림용 WS 연결 성공: " + frame);
      // 로그인한 사용자의 id는 #chatData에 담겨있어야 합니다.
      var currentUserId = parseInt($("#chatData").data("current-user-id"));
      notificationClient.subscribe(`/queue/notifications/${currentUserId}`, function(response){
        var unreadCount = parseInt(response.body) || 0;
        console.log("알림으로 받은 unread count:", unreadCount);
        $("#trainerUnreadCount").text(unreadCount);
        if(unreadCount > 0){
          $("#trainerUnreadBadge").text(unreadCount).show();
        } else {
          $("#trainerUnreadBadge").hide();
        }
      });
    }, function(error){
      console.error("알림용 WS 연결 오류:", error);
    });
  }
  connectNotificationWebSocket();

  // 3. "채팅 열기" 버튼 클릭 시 동작 (토글)
  $("#openChatBtn").click(function(){
    if ($("#floatingChatContainer").is(":visible")) {
      // 채팅창이 열려 있으면 닫고, 채팅용 WS 연결 종료
      $("#floatingChatContainer").hide();
      if (window.stompClient && window.stompClient.connected) {
        window.stompClient.disconnect(function() {
          console.log("채팅용 WS 연결 종료됨.");
        });
      }
    } else {
      // 채팅창이 닫혀 있으면, unread 배지를 초기화(읽음 처리)
      $("#trainerUnreadBadge").hide();
      $("#trainerUnreadCount").text("0");

      // 채팅 데이터를 #chatData에서 읽어옴 (이 값은 userschedule.html에 있어야 함)
      var chatData = $("#chatData");
      if (!chatData.length) {
        alert("채팅 데이터를 불러올 수 없습니다.");
        return;
      }
      window.currentUser = {
        userId: parseInt(chatData.data("current-user-id")),
        userName: chatData.data("current-user-name")
      };
      window.targetUser = {
        userId: parseInt(chatData.data("target-user-id")),
        userName: chatData.data("target-user-name")
      };
      var applicationId = chatData.data("application-id");
      console.log("로그인한 사용자:", window.currentUser);
      console.log("할당된 트레이너:", window.targetUser);
      console.log("applicationId:", applicationId);

      // AJAX를 통해 채팅 프래그먼트를 불러와 채팅창에 표시
      $.ajax({
        url: `/chat?applicationId=${encodeURIComponent(applicationId)}`,
        type: "GET",
        success: function(htmlFragment){
          $("#floatingChatContainer").html(htmlFragment);
          $("#floatingChatContainer").show();
          if (typeof initChat === "function") {
            initChat();
          }
          if (!window.stompClient || !window.stompClient.connected) {
            connectChat();
          }
        },
        error: function(xhr){
          alert("채팅창 불러오기 중 오류가 발생했습니다.");
          console.error("Error:", xhr);
        }
      });
    }
  });

  // 4. sendBtn 클릭 이벤트 (메시지 전송)
  $(document).on("click", "#sendBtn", function(){
    sendChatMessage();
  });

  window.sendChatMessage = function(){
    var msgInput = $("#newMessage");
    var message = msgInput.val().trim();
    if (!message || !window.targetUser || !window.targetUser.userId) return;
    var chatMessage = {
      senderId: window.currentUser.userId,
      receiverId: window.targetUser.userId,
      content: message
    };
    if (window.stompClient && window.stompClient.connected){
      window.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
    } else {
      console.error("채팅용 WS 연결이 되어있지 않습니다.");
    }
    msgInput.val("");
  };

  // 5. 채팅창용 WebSocket 연결 (채팅창이 열릴 때만 호출)
  function connectChat(){
    var socket = new SockJS('/ws');
    window.stompClient = Stomp.over(socket);
    window.stompClient.connect({}, function(frame){
      console.log("채팅용 WS 연결 성공:", frame);
      $("#sendBtn").prop("disabled", false);
      window.stompClient.subscribe(`/queue/chat/${window.currentUser.userId}`, function(response){
        var chat = JSON.parse(response.body);
        // 두 사용자 간의 메시지라면 채팅창 업데이트
        if (
            (chat.senderId === window.targetUser.userId && chat.receiverId === window.currentUser.userId) ||
            (chat.senderId === window.currentUser.userId && chat.receiverId === window.targetUser.userId)
        ) {
          updateChatWindow(chat);
          console.log("채팅 메시지 수신:", chat);
        } else {
          // 다른 메시지(예: 트레이너로부터 온 메시지)는 unread 배지 업데이트
          updateUnreadCount(chat.senderId);
        }
      });
    }, function(error){
      console.error("채팅용 WS 연결 오류:", error);
    });
  }

  // 6. 채팅창 UI 업데이트 함수
  function updateChatWindow(chat){
    var conversationArea = $("#conversationArea");
    var messageDiv = $("<div>").addClass("message");
    var senderName = (chat.senderId === window.currentUser.userId) ? window.currentUser.userName : window.targetUser.userName;
    messageDiv.html("<strong>" + senderName + "</strong>: <span>" + chat.content + "</span>");
    conversationArea.append(messageDiv);
    conversationArea.scrollTop(conversationArea.prop("scrollHeight"));
  }

  // 7. unread 메시지 업데이트 함수 (채팅창이 닫힌 상태에서 트레이너가 보낸 메시지 처리)
  function updateUnreadCount(senderId){
    if (window.targetUser && senderId === window.targetUser.userId) {
      var currentCount = parseInt($("#trainerUnreadBadge").text()) || 0;
      currentCount++;
      $("#trainerUnreadBadge").text(currentCount).show();
      $("#trainerUnreadCount").text(currentCount);
      console.log("업데이트된 unread count:", currentCount);
    }
  }
});
