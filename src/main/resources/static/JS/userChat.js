// userChat.js
$(document).ready(function() {

  //------------------------------------------------------
  // (1) 알림용 WebSocket 연결 (채팅창이 닫혀 있어도 unread 업데이트)
  //------------------------------------------------------
  function connectNotificationWebSocket() {
    const socket = new SockJS('/ws');
    const notificationClient = Stomp.over(socket);
    notificationClient.connect({}, function(frame) {
      console.log("알림용 WS 연결 성공:", frame);

      // 현재 로그인 유저의 ID -> #chatData의 data-current-user-id 등을 통해 가져오기
      const currentUserId = parseInt($("#chatData").data("current-user-id"));
      notificationClient.subscribe(`/queue/notifications/${currentUserId}`, function(response) {
        const unreadCount = parseInt(response.body) || 0;
        console.log("알림으로 받은 unreadCount:", unreadCount);

        // 화면에 unreadCount 배지 표시
        $("#trainerUnreadCount").text(unreadCount);
        if (unreadCount > 0) {
          $("#trainerUnreadBadge").text(unreadCount).show();
        } else {
          $("#trainerUnreadBadge").hide();
        }
      });

      // 트레이너 온라인 상태 구독 추가
      notificationClient.subscribe('/topic/onlineStatus', function(response) {
        const [action, trainerId] = response.body.split(":");
        const currentTrainerId = parseInt($("#chatData").data("target-user-id"));

        if (parseInt(trainerId) === currentTrainerId) {
          if (action === "LOGIN") {
            $("#trainerOnlineBadge").show();
            console.log("트레이너 로그인");
          } else if (action === "LOGOUT") {
            $("#trainerOnlineBadge").hide();
            console.log("트레이너 로그아웃");
          }
        }
      });

    }, function(error) {
      console.error("알림용 WS 연결 오류:", error);
    });
  }
  // 페이지 로드시 알림용 WebSocket 연결
  connectNotificationWebSocket();


  //------------------------------------------------------
  // (2) "채팅 열기" 버튼 (#openChatBtn) 클릭 시 (userschedule.html의 chat.png 버튼)
  //------------------------------------------------------
  $("#openChatBtn").click(function() {
    // 이미 채팅창이 열려 있으면 닫기
    if ($("#floatingChatContainer").is(":visible")) {
      $("#floatingChatContainer").hide();

      // 채팅용 WebSocket 연결 종료
      if (window.stompClient && window.stompClient.connected) {
        window.stompClient.disconnect(function() {
          console.log("채팅용 WS 연결 종료됨.");
        });
      }
    } else {
      // 채팅창 열기
      $("#floatingChatContainer").show();

      // unread 배지 초기화
      $("#trainerUnreadBadge").hide();
      $("#trainerUnreadCount").text("0");

      // #chatData에서 로그인 사용자 & 트레이너(대상) 정보 읽기
      const chatData = $("#chatData");
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
      const applicationId = chatData.data("application-id");
      console.log("현재 사용자:", window.currentUser);
      console.log("대상 트레이너:", window.targetUser);
      console.log("applicationId:", applicationId);

      // AJAX로 chatFragment를 로드
      $.ajax({
        url: `/chat?applicationId=${encodeURIComponent(applicationId)}`,
        type: "GET",
        success: function(htmlFragment) {
          // 받은 프래그먼트( chat.html )를 floatingChatContainer에 삽입
          $("#floatingChatContainer").html(htmlFragment);

          // chat.html 내부의 initChat() 호출
          if (typeof initChat === "function") {
            initChat();
          }

          // 채팅용 WebSocket 연결
          if (!window.stompClient || !window.stompClient.connected) {
            connectChat();
          }
        },
        error: function(xhr) {
          alert("채팅창 로드 중 오류가 발생했습니다.");
          console.error("Error:", xhr);
        }
      });
    }
  });


  //------------------------------------------------------
  // (3) 채팅창 WebSocket 연결
  //------------------------------------------------------
  function connectChat() {
    const socket = new SockJS('/ws');
    window.stompClient = Stomp.over(socket);
    window.stompClient.connect({}, function(frame) {
      console.log("채팅용 WS 연결 성공:", frame);
      $("#sendBtn").prop("disabled", false);

      // 현재 사용자 큐(/queue/chat/{userId}) 구독
      window.stompClient.subscribe(`/queue/chat/${window.currentUser.userId}`, function(response) {
        const chat = JSON.parse(response.body);

        // 현재 열려 있는 대화 (본인 <-> targetUser)인지 확인
        if (
            (chat.senderId === window.targetUser.userId && chat.receiverId === window.currentUser.userId) ||
            (chat.senderId === window.currentUser.userId && chat.receiverId === window.targetUser.userId)
        ) {
          // 대화창 업데이트
          updateChatWindow(chat);
          console.log("채팅 메시지 수신:", chat);
        } else {
          // 다른 사람이 보낸 메시지면 unread 배지 증가
          updateUnreadCount(chat.senderId);
        }
      });
    }, function(error) {
      console.error("채팅용 WS 연결 오류:", error);
    });
  }


  //------------------------------------------------------
  // (4) 전송 버튼(#sendBtn) 클릭 -> 텍스트 메시지 전송
  //------------------------------------------------------
  $(document).on("click", "#sendBtn", function() {
    sendChatMessage();
  });

  window.sendChatMessage = function() {
    const msgInput = $("#newMessage");
    const message = msgInput.val().trim();
    if (!message || !window.targetUser || !window.targetUser.userId) return;

    const chatMessage = {
      senderId: window.currentUser.userId,
      receiverId: window.targetUser.userId,
      content: message
    };
    if (window.stompClient && window.stompClient.connected) {
      // STOMP 전송
      window.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
    } else {
      console.error("채팅용 WS 연결이 되어있지 않습니다.");
    }
    msgInput.val("");
  };


  //------------------------------------------------------
  // (5) 메시지 수신 시 UI 반영
  //------------------------------------------------------
  function updateChatWindow(chat) {
    const conversationArea = $("#conversationArea");
    const messageDiv = $("<div>").addClass("message");

    // 발신자 이름
    const senderName = (chat.senderId === window.currentUser.userId)
        ? window.currentUser.userName
        : window.targetUser.userName;

    // 파일 메시지 vs 텍스트 메시지
    if (chat.fileUrl && chat.fileType) {
      let filePreview = "";
      switch (chat.fileType) {
        case "image":
          filePreview = `<img src="${chat.fileUrl}" alt="이미지" style="max-width:200px;">`;
          break;
        case "video":
          filePreview = `
            <video controls width="300">
              <source src="${chat.fileUrl}" type="video/mp4"/>
              영상 재생 불가
            </video>`;
          break;
        case "audio":
          filePreview = `
            <audio controls>
              <source src="${chat.fileUrl}" type="audio/mpeg"/>
              오디오 재생 불가
            </audio>`;
          break;
        default:
          const docName = chat.originalFileName || "파일";
          filePreview = `<a href="${chat.fileUrl}" download="${docName}">${docName}</a>`;
          break;
      }
      messageDiv.html(`<strong>${senderName}</strong>: ${filePreview}`);
    } else {
      // 일반 텍스트
      messageDiv.html(`<strong>${senderName}</strong>: <span>${chat.content}</span>`);
    }

    conversationArea.append(messageDiv);
    conversationArea.scrollTop(conversationArea.prop("scrollHeight"));
  }

  // 대화중이 아닌 메시지 -> unread 배지 증가
  function updateUnreadCount(senderId) {
    if (window.targetUser && senderId === window.targetUser.userId) {
      return;
    }
    let currentCount = parseInt($("#trainerUnreadBadge").text()) || 0;
    currentCount++;
    $("#trainerUnreadBadge").text(currentCount).show();
    $("#trainerUnreadCount").text(currentCount);
    console.log("업데이트된 unread count:", currentCount);
  }


  //------------------------------------------------------
  // (6) 아이콘 + file input => 파일 업로드
  //------------------------------------------------------
  // 아이콘(#fileSelectIcon) 클릭 -> 숨긴 file input(#fileInput).click()
  $(document).on("click", "#fileSelectIcon", function() {
    $("#fileInput").click();
  });

  // 파일 선택 -> 업로드
  $(document).on("change", "#fileInput", function(e) {
    const file = e.target.files[0];
    if (!file) return;
    uploadChatFile(file);
    e.target.value = ""; // 다시 선택 가능하도록 초기화
  });

  function uploadChatFile(file) {
    const formData = new FormData();
    formData.append("uploadFile", file);

    fetch("/chat/uploadFile", {
      method: "POST",
      body: formData
    })
    .then(resp => resp.json())
    .then(result => {
      // 업로드 성공 -> STOMP 전송
      const chatMessage = {
        senderId: window.currentUser.userId,
        receiverId: window.targetUser.userId,
        content: "",
        originalFileName: result.originalFileName,
        savedFileName: result.savedFileName,
        fileType: result.fileType,
        fileUrl: result.fileUrl
      };
      if (window.stompClient && window.stompClient.connected) {
        window.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
      } else {
        console.error("WS 연결이 안 되어있습니다.");
      }
    })
    .catch(err => {
      console.error("파일 업로드 실패:", err);
    });
  }

});
