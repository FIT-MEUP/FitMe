// memberManage.js
$(document).ready(function () {
  // "수락" 버튼 클릭 시
  $(".approve-btn").click(function () {
    let applicationId = $(this).data("id");
    console.log(applicationId);
    $.ajax({
      url: `/trainer/approve`,
      type: "POST",
      contentType: "application/json",
      data: JSON.stringify({ applicationId: applicationId }),
      success: function (response) {
        alert("신청이 승인되었습니다.");
        location.reload();
      },
      error: function (xhr) {
        alert("승인 중 오류가 발생했습니다.");
        console.error("Error:", xhr);
      }
    });
  });

  // "거절" 버튼 클릭 시
  $(".reject-btn").click(function () {
    let applicationId = $(this).data("id");
    $.ajax({
      url: `/trainer/reject`,
      type: "POST",
      contentType: "application/json",
      data: JSON.stringify({ applicationId: applicationId }),
      success: function (response) {
        alert("신청이 거절되었습니다.");
        location.reload();
      },
      error: function (xhr) {
        alert("거절 중 오류가 발생했습니다.");
        console.error("Error:", xhr);
      }
    });
  });

  // 관리 회원 목록 클릭 시
  $(".select-btn").click(function () {
    let applicationId = $(this).data("id");
    console.log("선택한 applicationId:", applicationId);

    // 회원 정보 조회 (선택된 신청서의 정보를 출력)
    $.ajax({
      url: `/trainer/select?applicationId=${applicationId}`,
      type: "GET",
      success: function (response) {
        alert("회원 정보 불러오기 성공");
        console.log(response);
        showUserInfo(response);
      },
      error: function (xhr) {
        alert("회원 정보 조회 중 오류가 발생했습니다.");
        console.error("Error:", xhr);
      }
    });
    window.targetApplicationId = applicationId;
    console.log("선택한 회원의 applicationId:", applicationId);

    // AJAX를 통해 채팅 프래그먼트 불러오기, 파라미터로 applicationId 전달
    $.ajax({
      url: `/chat?applicationId=${encodeURIComponent(applicationId)}`,
      type: "GET",
      success: function (htmlFragment) {
        $("#chatFragmentContainer").html(htmlFragment);
        $("#chatFragmentContainer").show();
        if (typeof initChat === "function") {
          initChat();
        }
        // hidden div #chatData에서 currentUser와 targetUser 정보를 읽어서 저장
        var chatData = $("#chatData");
        if(chatData.length) {
          window.currentUser = {
            userId: parseInt(chatData.data("current-user-id")),
            userName: chatData.data("current-user-name")
          };
          window.targetUser = {
            userId: parseInt(chatData.data("target-user-id")),
            userName: chatData.data("target-user-name")
          };
          console.log("로그인한 사용자:", window.currentUser);
          console.log("대상 회원:", window.targetUser);
        }
        if (!window.stompClient || !window.stompClient.connected) {
          connectChat();
        }
      },
      error: function (xhr) {
        alert("채팅창 로드 중 오류가 발생했습니다.");
        console.error("Error:", xhr);
      }
    });
  });

  // 동적 요소인 #sendBtn 이벤트 위임
  $(document).on("click", "#sendBtn", function () {
    window.sendChatMessage();
  });

  // 메시지 전송 함수
  window.sendChatMessage = function () {
    const msgInput = $("#newMessage");
    const message = msgInput.val().trim();
    if (!message || !window.targetUser || !window.targetUser.userId) return;
    const chatMessage = {
      senderId: window.currentUser.userId,
      receiverId: window.targetUser.userId,
      content: message
    };
    if (window.stompClient && window.stompClient.connected) {
      window.stompClient.send("/app/chat.sendMessage", {},
          JSON.stringify(chatMessage));
      // updateChatWindow(chatMessage); // 서버 응답을 통해 UI 업데이트되도록 함
    } else {
      console.error("WS 연결이 되어있지 않습니다.");
    }
    msgInput.val("");
  };

  // WebSocket 연결 함수
  function connectChat() {
    const socket = new SockJS('/ws');
    window.stompClient = Stomp.over(socket);
    window.stompClient.connect({}, function (frame) {
      console.log('WS 연결 성공: ' + frame);
      $("#sendBtn").prop("disabled", false);
      window.stompClient.subscribe(`/queue/chat/${window.currentUser.userId}`, function (response) {
        const chat = JSON.parse(response.body);
        if (
            (chat.senderId === window.targetUser.userId && chat.receiverId === window.currentUser.userId) ||
            (chat.senderId === window.currentUser.userId && chat.receiverId === window.targetUser.userId)
        ) {
          updateChatWindow(chat);
          console.log("currentUser:", window.currentUser);
        } else {
          updateUnreadCount(chat.senderId);
        }
      });
    }, function (error) {
      console.error("WS 연결 오류:", error);
    });
  }

  function updateUnreadCount(userId) {
    const sel = `.user-item[data-user-id="${userId}"] .unread-count`;
    const unreadSpan = $(sel);
    if (unreadSpan.length) {
      let val = parseInt(unreadSpan.text()) || 0;
      unreadSpan.text(val + 1);
    }
  }

  function updateChatWindow(chat) {
    const conversationArea = $("#conversationArea");
    const messageDiv = $("<div>").addClass("message");
    let senderName = (chat.senderId === window.currentUser.userId)
        ? window.currentUser.userName
        : window.targetUser.userName;
    messageDiv.html(`<strong>${senderName}</strong>: <span>${chat.content}</span>`);
    conversationArea.append(messageDiv);
    conversationArea.scrollTop(conversationArea.prop("scrollHeight"));
  }
});

function showUserInfo(response) {
  const $userInfoDiv = $("#userInfo");
  console.log(response);
  $userInfoDiv.html(`
    <h3 class="text-2xl font-semibold text-left px-2 py-1 text-black"> ${response}</h3>
  `);
}
