// userChat.js
$(document).ready(function(){
  $("#openChatBtn").click(function(){
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

    // AJAX를 통해 chat.html 프래그먼트 불러오기 (파라미터로 applicationId 전달)
    $.ajax({
      url: `/chat?applicationId=${encodeURIComponent(applicationId)}`,
      type: "GET",
      success: function(htmlFragment){
        $("#floatingChatContainer").html(htmlFragment);
        $("#floatingChatContainer").show();
        if (typeof initChat === "function") {
          initChat();
        }
        // 이전 웹소켓 연결이 있다면 해제하고 새 연결 생성
        if (window.stompClient && window.stompClient.connected) {
          window.stompClient.disconnect(function() {
            console.log("이전 WebSocket 연결 해제됨.");
            connectChat();
          });
        } else {
          connectChat();
        }
      },
      error: function(xhr){
        alert("채팅창 불러오기 중 오류가 발생했습니다.");
        console.error("Error:", xhr);
      }
    });
  });

  $(document).on("click", "#sendBtn", function(){
    sendChatMessage();
  });

  window.sendChatMessage = function(){
    var msgInput = $("#newMessage");
    var message = msgInput.val().trim();
    if(!message || !window.targetUser || !window.targetUser.userId) return;
    var chatMessage = {
      senderId: window.currentUser.userId,
      receiverId: window.targetUser.userId,
      content: message
    };
    if(window.stompClient && window.stompClient.connected){
      window.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
      // 서버의 echo를 통해 메시지가 UI에 표시되므로 여기서는 UI 업데이트를 하지 않습니다.
    } else {
      console.error("WS 연결이 되어있지 않습니다.");
    }
    msgInput.val("");
  };

  function connectChat(){
    var socket = new SockJS('/ws');
    window.stompClient = Stomp.over(socket);
    window.stompClient.connect({}, function(frame){
      console.log("WS 연결 성공: " + frame);
      $("#sendBtn").prop("disabled", false);
      window.stompClient.subscribe(`/queue/chat/${window.currentUser.userId}`, function(response){
        var chat = JSON.parse(response.body);
        // 두 사용자 간의 메시지이면 UI 업데이트
        if (
            (chat.senderId === window.targetUser.userId && chat.receiverId === window.currentUser.userId) ||
            (chat.senderId === window.currentUser.userId && chat.receiverId === window.targetUser.userId)
        ) {
          updateChatWindow(chat);
          console.log("메시지 수신:", chat);
        } else {
          updateUnreadCount(chat.senderId);
        }
      });
    }, function(error){
      console.error("WS 연결 오류:", error);
    });
  }

  function updateChatWindow(chat){
    var conversationArea = $("#conversationArea");
    var messageDiv = $("<div>").addClass("message");
    var senderName = (chat.senderId === window.currentUser.userId) ? window.currentUser.userName : window.targetUser.userName;
    messageDiv.html("<strong>" + senderName + "</strong>: <span>" + chat.content + "</span>");
    conversationArea.append(messageDiv);
    conversationArea.scrollTop(conversationArea.prop("scrollHeight"));
  }

  function updateUnreadCount(userId){
    // 미읽은 메시지 처리는 필요 시 구현
  }
});
