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

          
                // ✅ 채팅창이 로드된 직후 스크롤을 가장 아래로 이동
                setTimeout(function() {
                  const conversationArea = document.getElementById("conversationArea");
                  if (conversationArea) {
                      conversationArea.scrollTop = conversationArea.scrollHeight;
                  }
              }, 1); // 100ms 딜레이를 주어 렌더링 완료를 기다림

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
    const conversationArea = document.getElementById("conversationArea");

    // 발신자에 따라 전체 컨테이너의 정렬 지정
    const messageContainer = document.createElement("div");
    if (chat.senderId === window.currentUser.userId) {
      messageContainer.className = "flex flex-col text-right";
    } else {
      messageContainer.className = "flex flex-col text-left";
    }

    // 말풍선 스타일을 적용할 내부 div 생성
    const messageBubble = document.createElement("div");
    if (chat.senderId === window.currentUser.userId) {
      messageBubble.className =
          "bg-green-400 text-white rounded-lg p-2 w-auto max-w-[150px] break-words whitespace-normal rightbox ml-auto inline-block text-left";
      // 발신자 이름 추가
      const messageSender = document.createElement("strong");
      messageSender.textContent = window.currentUser.userName;
      messageContainer.appendChild(messageSender);
    } else {
      messageBubble.className =
          "bg-white text-gray-900 rounded-lg p-2 w-fit max-w-[150px] border break-words whitespace-normal leftbox inline-block text-left";
      // 수신자 이름 추가
      const messageSender = document.createElement("strong");
      messageSender.textContent = window.targetUser.userName;

      messageContainer.appendChild(messageSender);
    }

    // 파일 메시지 처리: 파일 미리보기와 다운로드 링크 추가
    if (chat.fileUrl && chat.fileType) {
      // 파일 미리보기용 컨테이너 생성 (전체 말풍선의 90% 폭, 가운데 정렬)
      const filePreviewContainer = document.createElement("div");
      filePreviewContainer.style.width = "90%";
      filePreviewContainer.style.margin = "0 auto";
      let filePreviewHTML = "";
      switch (chat.fileType) {
        case "image":
          filePreviewHTML = `<img src="${chat.fileUrl}" alt="이미지" style="max-width:100%; display:block;">`;
          break;
        case "video":
          filePreviewHTML = `<video controls style="max-width:100%; display:block;">
                                      <source src="${chat.fileUrl}" type="video/mp4">
                                      영상 재생 불가
                                   </video>`;
          break;
        case "audio":
          filePreviewHTML = `<audio controls style="max-width:100%; display:block;">
                                      <source src="${chat.fileUrl}" type="audio/mpeg">
                                      오디오 재생 불가
                                   </audio>`;
          break;
        default:
          filePreviewHTML = `<span>파일 미리보기 불가</span>`;
          break;
      }
      filePreviewContainer.innerHTML = filePreviewHTML;

      // 다운로드 링크 생성 (말풍선 아래에 위치)
      const downloadLink = document.createElement("a");
      downloadLink.href = chat.fileUrl;
      downloadLink.download = chat.originalFileName || "파일";
      downloadLink.textContent = chat.originalFileName ? "다운로드" : "파일 다운로드";
      downloadLink.style.display = "block";
      downloadLink.style.marginTop = "5px";
      downloadLink.style.fontSize = "12px";

      // 말풍선 안에 미리보기와 다운로드 링크 추가
      messageBubble.appendChild(filePreviewContainer);
      messageBubble.appendChild(downloadLink);
    } else {
      // 텍스트 메시지 처리
      const messageText = document.createElement("span");
      messageText.className = "leading-5";
      messageText.textContent = chat.content;
      messageBubble.appendChild(messageText);
    }

    // 구성 요소 결합 및 추가
    messageContainer.appendChild(messageBubble);
    conversationArea.appendChild(messageContainer);
    conversationArea.scrollTop = conversationArea.scrollHeight;
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
