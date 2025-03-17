// memberManage.js
$(document).ready(function () {

  // ★ 추가: 트레이너용 알림 WebSocket 연결 함수 (항상 연결되어 있음)
  function connectTrainerNotificationWebSocket() {
    var socket = new SockJS('/ws');
    var notificationClient = Stomp.over(socket);
    notificationClient.connect({}, function (frame) {
      console.log("Trainer 알림용 WS 연결 성공:", frame);
      // 최상위 container에 저장된 trainer의 userId 사용 (예: .container의 data-user-name에 trainer의 userId가 저장되어 있다고 가정)
      var trainerUserId = parseInt($(".container").data("user-name"));
      console.log("트레이너 userId:", trainerUserId);
      notificationClient.subscribe(`/queue/notifications/${trainerUserId}`, function (response) {
        try {
          // 서버가 JSON 형태로 { userId1: unreadCount1, userId2: unreadCount2, ... } 전송
          var unreadMap = JSON.parse(response.body);
          console.log("알림으로 받은 unreadMap:", unreadMap);
          // 각 회원 목록(li) 업데이트: 만약 현재 선택된 대화(targetUser)가 있다면 그 유저는 업데이트하지 않음
          $(".memberList li").each(function () {
            var userId = $(this).find(".select-btn").data("user-id");
            // 현재 대화중인 유저와 다르면 unread count를 업데이트
            if (!window.targetUser || window.targetUser.userId != userId) {
              var unreadCount = unreadMap[userId] || 0;
              if (unreadCount > 0) {
                $(this).find(".unread-count").text(unreadCount).show();
              } else {
                $(this).find(".unread-count").text("").hide();
              }
            }
          });
        } catch (e) {
          console.error("알림 데이터 파싱 오류:", e);
        }
      });
    }, function (error) {
      console.error("Trainer 알림용 WS 연결 오류:", error);
    });
  }

  // 페이지 로드시 트레이너 알림용 WS 연결 실행
  connectTrainerNotificationWebSocket();


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
    let userId = $(this).data("user-id");

    updateUnreadCountToZero(userId);

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
        // 채팅용 웹소캣 연결: 기존 연결이 있으면 해제 후 재연결
        if (!window.stompClient || !window.stompClient.connected) {
          connectChat();
        } else {
          window.stompClient.disconnect(function() {
            console.log("이전 채팅용 WS 연결 해제됨.");
            connectChat();
          });
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

  // 채팅용 WebSocket 연결 함수 (대화창 열릴 때 호출)
  function connectChat() {
    const socket = new SockJS('/ws');
    window.stompClient = Stomp.over(socket);
    window.stompClient.connect({}, function (frame) {
      console.log('채팅용 WS 연결 성공: ' + frame);
      $("#sendBtn").prop("disabled", false);
      window.stompClient.subscribe(`/queue/chat/${window.currentUser.userId}`, function (response) {
        const chat = JSON.parse(response.body);
        // 만약 현재 대화중인 대상과의 메시지라면 채팅창에 표시하고, unread 배지는 초기화
        if (
            (chat.senderId === window.targetUser.userId && chat.receiverId === window.currentUser.userId) ||
            (chat.senderId === window.currentUser.userId && chat.receiverId === window.targetUser.userId)
        ) {
          updateChatWindow(chat);
          console.log("채팅 메시지 수신:", chat);
        } else {
          // 현재 대화중이 아닌 경우에만 unread 배지를 업데이트
          updateUnreadCount(chat.senderId);
        }
      });
    }, function (error) {
      console.error("채팅용 WS 연결 오류:", error);
    });
  }

  // 채팅창 UI 업데이트 함수
  function updateChatWindow(chat) {
    const conversationArea = $("#conversationArea");
    const messageDiv = $("<div>").addClass("message");
    let senderName = (chat.senderId === window.currentUser.userId) ? window.currentUser.userName : window.targetUser.userName;
    messageDiv.html(`<strong>${senderName}</strong>: <span>${chat.content}</span>`);
    conversationArea.append(messageDiv);
    conversationArea.scrollTop(conversationArea.prop("scrollHeight"));
  }

  // unread 메시지 업데이트 함수 (대화창이 닫힌 상태에서)
  function updateUnreadCount(senderId) {
    // 만약 현재 열려 있는 대화 대상이 senderId와 동일하면 업데이트하지 않음
    if (window.targetUser && senderId === window.targetUser.userId) {
      return;
    }
    // 해당 회원의 unread 배지를 찾아서 증가
    const sel = `.select-btn[data-user-id="${senderId}"] .unread-count`;
    const unreadSpan = $(sel);
    if (unreadSpan.length) {
      let val = parseInt(unreadSpan.text()) || 0;
      unreadSpan.text(val + 1).show();
    }
  }

  // 대화창이 열릴 때 선택한 회원의 unread 배지 초기화 함수
  function updateUnreadCountToZero(userId) {
    const sel = `.select-btn[data-user-id="${userId}"] .unread-count`;
    $(sel).text("").hide();
  }
});

function showUserInfo(response) {
  const $userInfoDiv = $("#userInfo");
  console.log(response);
  $userInfoDiv.html(`
    <h3 class="text-2xl font-semibold text-left px-2 py-1 text-black"> ${response}</h3>
  `);
}
