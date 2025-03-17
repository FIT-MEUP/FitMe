// memberManage.js
$(document).ready(function () {

  //------------------------------------------------------
  // (1) 트레이너 알림용 WebSocket 연결
  //------------------------------------------------------
  function connectTrainerNotificationWebSocket() {
    var socket = new SockJS('/ws');
    var notificationClient = Stomp.over(socket);
    notificationClient.connect({}, function (frame) {
      console.log("Trainer 알림용 WS 연결 성공:", frame);
      // 최상위 .container에 저장된 trainer의 userId 사용
      var trainerUserId = parseInt($(".container").data("user-name"));
      console.log("트레이너 userId:", trainerUserId);
      notificationClient.subscribe(`/queue/notifications/${trainerUserId}`, function (response) {
        try {
          var unreadMap = JSON.parse(response.body);
          console.log("알림으로 받은 unreadMap:", unreadMap);
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

  // 페이지 로드시 트레이너 알림용 WS 연결
  connectTrainerNotificationWebSocket();


  //------------------------------------------------------
  // (2) 신청 승인/거절 처리
  //------------------------------------------------------
  $(".approve-btn").click(function () {
    let applicationId = $(this).data("id");
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


  //------------------------------------------------------
  // (3) 회원 목록 클릭 -> 대화 로드
  //------------------------------------------------------
  $(".select-btn").click(function () {
    let applicationId = $(this).data("id");
    let userId = $(this).data("user-id");

    updateUnreadCountToZero(userId);
    console.log("선택한 applicationId:", applicationId);

    // (A) 회원 정보 조회
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

    // (B) 채팅 프래그먼트 로드
    $.ajax({
      url: `/chat?applicationId=${encodeURIComponent(applicationId)}`,
      type: "GET",
      success: function (htmlFragment) {
        $("#chatFragmentContainer").html(htmlFragment);
        $("#chatFragmentContainer").show();

        // chat.html의 initChat() 호출
        if (typeof initChat === "function") {
          initChat();
        }

        // hidden div #chatData에서 currentUser, targetUser 정보 읽음
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
          console.log("로그인(트레이너):", window.currentUser);
          console.log("대상 회원:", window.targetUser);
        }

        // 채팅용 웹소켓 연결
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


  //------------------------------------------------------
  // (4) sendBtn 클릭 -> 텍스트 메시지 전송
  //------------------------------------------------------
  $(document).on("click", "#sendBtn", function () {
    window.sendChatMessage();
  });

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
      // STOMP 전송
      window.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));

      // (★1) 트레이너도 "local UI" 즉시 반영
      updateChatWindow(chatMessage);
    } else {
      console.error("WS 연결이 안 되어있습니다.");
    }
    msgInput.val("");
  };


  //------------------------------------------------------
  // (5) 채팅용 WebSocket 연결 함수
  //------------------------------------------------------
  function connectChat() {
    const socket = new SockJS('/ws');
    window.stompClient = Stomp.over(socket);
    window.stompClient.connect({}, function (frame) {
      console.log('채팅용 WS 연결 성공: ' + frame);
      $("#sendBtn").prop("disabled", false);
      window.stompClient.subscribe(`/queue/chat/${window.currentUser.userId}`, function (response) {
        const chat = JSON.parse(response.body);

        // (★2) 서버 echo에 대한 조건문 (수신 vs 발신)
        // 내가 발신한 메시지도 여기를 통해 들어올 수 있음
        if (
            (chat.senderId === window.targetUser.userId && chat.receiverId === window.currentUser.userId) ||
            (chat.senderId === window.currentUser.userId && chat.receiverId === window.targetUser.userId)
        ) {
          updateChatWindow(chat);
          console.log("채팅 메시지 수신:", chat);
        } else {
          // 현재 대화중이 아니면 unread 배지
          updateUnreadCount(chat.senderId);
        }
      });
    }, function (error) {
      console.error("채팅용 WS 연결 오류:", error);
    });
  }


  //------------------------------------------------------
  // (6) 채팅창 UI 업데이트 함수 (파일/텍스트 공용)
  //------------------------------------------------------
  function updateChatWindow(chat) {
    const conversationArea = $("#conversationArea");
    const messageDiv = $("<div>").addClass("message");

    let senderName = (chat.senderId === window.currentUser.userId)
        ? window.currentUser.userName
        : window.targetUser.userName;

    // 파일 vs 텍스트 메시지
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
              영상 불가
            </video>`;
          break;
        case "audio":
          filePreview = `
            <audio controls>
              <source src="${chat.fileUrl}" type="audio/mpeg"/>
              오디오 불가
            </audio>`;
          break;
        default:
          const docName = chat.originalFileName || "파일";
          filePreview = `<a href="${chat.fileUrl}" download="${docName}">${docName}</a>`;
          break;
      }
      messageDiv.html(`<strong>${senderName}</strong>: ${filePreview}`);
    } else {
      messageDiv.html(`<strong>${senderName}</strong>: <span>${chat.content}</span>`);
    }

    conversationArea.append(messageDiv);
    conversationArea.scrollTop(conversationArea.prop("scrollHeight"));
  }


  //------------------------------------------------------
  // (7) 미읽음 배지 업데이트
  //------------------------------------------------------
  function updateUnreadCount(senderId) {
    if (window.targetUser && senderId === window.targetUser.userId) {
      return;
    }
    const sel = `.select-btn[data-user-id="${senderId}"] .unread-count`;
    const unreadSpan = $(sel);
    if (unreadSpan.length) {
      let val = parseInt(unreadSpan.text()) || 0;
      unreadSpan.text(val + 1).show();
    }
  }

  function updateUnreadCountToZero(userId) {
    const sel = `.select-btn[data-user-id="${userId}"] .unread-count`;
    $(sel).text("").hide();
  }


  //------------------------------------------------------
  // (8) 회원 정보 표시
  //------------------------------------------------------
  window.showUserInfo = function(response) {
    const $userInfoDiv = $("#userInfo");
    console.log(response);
    $userInfoDiv.html(`
      <h3 class="text-2xl font-semibold text-left px-2 py-1 text-black"> ${response}</h3>
    `);
  };


  //------------------------------------------------------
  // (9) 파일 업로드 (plus.png + #fileInput)
  //------------------------------------------------------
  // 아이콘 클릭 => 숨겨진 파일 input 열기
  $(document).on("click", "#fileSelectIcon", function() {
    $("#fileInput").click();
  });

  // 파일 선택 -> 업로드
  $(document).on("change", "#fileInput", function(e) {
    const file = e.target.files[0];
    if (!file) return;
    uploadChatFile(file);
    e.target.value = "";
  });

  function uploadChatFile(file) {
    let formData = new FormData();
    formData.append("uploadFile", file);

    fetch("/chat/uploadFile", {
      method: "POST",
      body: formData
    })
    .then(resp => resp.json())
    .then(result => {
      // 업로드 성공 => STOMP로 파일 메시지 전송
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
