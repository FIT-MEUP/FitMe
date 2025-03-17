// memberManage.js
$(document).ready(function () {
  // ★ 추가: 트레이너용 알림 WebSocket 연결 함수 (항상 연결되어 있음)
  function connectTrainerNotificationWebSocket() {
    var socket = new SockJS("/ws");
    var notificationClient = Stomp.over(socket);
    notificationClient.connect(
      {},
      function (frame) {
        console.log("Trainer 알림용 WS 연결 성공:", frame);
        // 최상위 container에 저장된 trainer의 userId 사용 (예: .container의 data-user-name에 trainer의 userId가 저장되어 있다고 가정)
        var trainerUserId = parseInt($(".container").data("user-name"));
        console.log("트레이너 userId:", trainerUserId);
        notificationClient.subscribe(
          `/queue/notifications/${trainerUserId}`,
          function (response) {
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
          }
        );
      },
      function (error) {
        console.error("Trainer 알림용 WS 연결 오류:", error);
      }
    );
  }

  // 페이지 로드시 트레이너 알림용 WS 연결 실행
  connectTrainerNotificationWebSocket();

  const noticeText = $("#noticeText");
  const editNotice = $("#editNotice");
  const editBtn = $("#editBtn");
  const saveBtn = $("#saveBtn");
  const cancelBtn = $("#cancelBtn");

  // 수정 버튼 클릭 시
  editBtn.click(function () {
    editNotice.val(noticeText.text());
    noticeText.addClass("hidden");
    editNotice.removeClass("hidden");
    editBtn.addClass("hidden");
    saveBtn.removeClass("hidden");
    cancelBtn.removeClass("hidden");
  });

  // 취소 버튼 클릭 시
  cancelBtn.click(function () {
    noticeText.removeClass("hidden");
    editNotice.addClass("hidden");
    editBtn.removeClass("hidden");
    saveBtn.addClass("hidden");
    cancelBtn.addClass("hidden");
  });

  // 완료(저장) 버튼 클릭 시
  saveBtn.click(function () {
    const updatedNotice = editNotice.val().trim();
    if (updatedNotice === "") {
      alert("공지사항 내용을 입력해주세요.");
      return;
    }

    // 서버로 데이터 전송
    $.ajax({
      url: "/trainer/saveAnnouncement",
      type: "POST",
      contentType: "application/json",
      data: updatedNotice,
      success: function (data) {
        console.log("data: " + data);
        if (data) {
          noticeText.text(updatedNotice);
          noticeText.removeClass("hidden");
          editNotice.addClass("hidden");
          editBtn.removeClass("hidden");
          saveBtn.addClass("hidden");
          cancelBtn.addClass("hidden");
        } else {
          alert("공지사항 업데이트에 실패했습니다.");
        }
      },
      error: function () {
        alert("서버 오류가 발생했습니다.");
      },
    });
  });

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
      },
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
      },
    });
  });

  // 관리 회원 목록 클릭 시
  $(".select-btn").click(function () {
    let applicationId = $(this).data("id");
    let userId = $(this).data("user-id");

    // 식단 버튼과 운동 버튼 보이기
    $("#diet-section button")
      .removeClass("hidden")
      .off("click")
      .on("click", function () {
        // 원하는 URL로 이동 (예: '/dietBoard'로 이동하면서 userId를 쿼리 파라미터로 전달)
        window.location.href = `/meals?userId=${userId}`;
      });

    $("#workout-section button")
      .removeClass("hidden")
      .off("click")
      .on("click", function () {
        // 원하는 URL로 이동 (예: '/workoutBoard'로 이동하면서 userId를 쿼리 파라미터로 전달)
        window.location.href = `/work?userId=${userId}`;
      });

    updateUnreadCountToZero(userId);

    console.log("선택한 applicationId:", applicationId);

    // 회원 정보 조회 (선택된 신청서의 정보를 출력)
    $.ajax({
      url: `/trainer/selectPT?userId=${userId}`,
      type: "GET",
      success: function (response) {
        showUserInfo(response, userId);
      },
      error: function (xhr) {
        alert("회원 정보 조회 중 오류가 발생했습니다.");
        console.error("Error:", xhr);
      },
    });

    $.ajax({
      url: `/trainer/mealPreview?userId=${userId}`,
      type: "GET",
      success: function (response) {
        showMealsInfo(response);
      },
      error: function (xhr) {
        noMealsInfo();
      },
    });

    $.ajax({
      url: `/trainer/workPreview?userId=${userId}`,
      type: "GET",
      success: function (response) {
        showWorkInfo(response);
      },
      error: function (xhr) {
        alert("실패");
        noWorkInfo();
      },
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
        if (chatData.length) {
          window.currentUser = {
            userId: parseInt(chatData.data("current-user-id")),
            userName: chatData.data("current-user-name"),
          };
          window.targetUser = {
            userId: parseInt(chatData.data("target-user-id")),
            userName: chatData.data("target-user-name"),
          };
          console.log("로그인한 사용자:", window.currentUser);
          console.log("대상 회원:", window.targetUser);
        }
        // 채팅용 웹소캣 연결: 기존 연결이 있으면 해제 후 재연결
        if (!window.stompClient || !window.stompClient.connected) {
          connectChat();
        } else {
          window.stompClient.disconnect(function () {
            console.log("이전 채팅용 WS 연결 해제됨.");
            connectChat();
          });
        }
      },
      error: function (xhr) {
        alert("채팅창 로드 중 오류가 발생했습니다.");
        console.error("Error:", xhr);
      },
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
      content: message,
    };
    if (window.stompClient && window.stompClient.connected) {
      window.stompClient.send(
        "/app/chat.sendMessage",
        {},
        JSON.stringify(chatMessage)
      );
      // updateChatWindow(chatMessage); // 서버 응답을 통해 UI 업데이트되도록 함
    } else {
      console.error("WS 연결이 되어있지 않습니다.");
    }
    msgInput.val("");
  };

  // 채팅용 WebSocket 연결 함수 (대화창 열릴 때 호출)
  function connectChat() {
    const socket = new SockJS("/ws");
    window.stompClient = Stomp.over(socket);
    window.stompClient.connect(
      {},
      function (frame) {
        console.log("채팅용 WS 연결 성공: " + frame);
        $("#sendBtn").prop("disabled", false);
        window.stompClient.subscribe(
          `/queue/chat/${window.currentUser.userId}`,
          function (response) {
            const chat = JSON.parse(response.body);
            // 만약 현재 대화중인 대상과의 메시지라면 채팅창에 표시하고, unread 배지는 초기화
            if (
              (chat.senderId === window.targetUser.userId &&
                chat.receiverId === window.currentUser.userId) ||
              (chat.senderId === window.currentUser.userId &&
                chat.receiverId === window.targetUser.userId)
            ) {
              updateChatWindow(chat);
              console.log("채팅 메시지 수신:", chat);
            } else {
              // 현재 대화중이 아닌 경우에만 unread 배지를 업데이트
              updateUnreadCount(chat.senderId);
            }
          }
        );
      },
      function (error) {
        console.error("채팅용 WS 연결 오류:", error);
      }
    );
  }

  // 채팅창 UI 업데이트 함수
  function updateChatWindow(chat) {
    const conversationArea = $("#conversationArea");
    const messageDiv = $("<div>").addClass("message");
    let senderName =
      chat.senderId === window.currentUser.userId
        ? window.currentUser.userName
        : window.targetUser.userName;
    messageDiv.html(
      `<strong>${senderName}</strong>: <span>${chat.content}</span>`
    );
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

// 보여질 정보들
function showUserInfo(response, userId) {
  const $userInfoDiv = $("#userInfo");

  $userInfoDiv.html(`
      <table class="w-full border border-gray-300">
          <tr>
              <th class="border px-4 py-2 text-left">남은 PT</th>
              <td class="border px-4 py-2 text-center" id="ptAmount">${response.changeAmount}</td>
              <td class="border px-4 py-2 text-center">
                  <button id="PTeditBtn" class="bg-blue-500 text-white px-4 py-2 rounded" onclick="editPT(${response.changeAmount}, ${userId})">수정</button>
              </td>
          </tr>
      </table>
  `);
}

function editPT(currentAmount, userId) {
  const $ptAmount = $("#ptAmount");
  const $PTeditBtn = $("#PTeditBtn");

  // 수정 모드로 변경
  $ptAmount.html(`
      <input type="number" id="newPTAmount" class="border px-2 py-1 w-20" value="${currentAmount}">
      <input type="text" id="reason" class="border px-2 py-1 w-40" placeholder="변경 사유 입력">
  `);

  $PTeditBtn.replaceWith(`
      <button id="PTsaveBtn" class="bg-green-500 text-white px-4 py-2 rounded" onclick="savePT(${userId})">확인</button>
      <button id="PTcancelBtn" class="bg-red-500 text-white px-4 py-2 rounded" onclick="cancelEdit(${currentAmount})">취소</button>
  `);
}

function savePT(userId) {
  const newPTAmount = $("#newPTAmount").val();
  const reason = $("#reason").val();

  if (!reason.trim()) {
    alert("변경 사유를 입력해주세요.");
    return;
  }

  $.ajax({
    url: `/trainer/updatePT`,
    type: "POST",
    contentType: "application/json",
    data: JSON.stringify({
      userId: userId,
      changeAmount: newPTAmount,
      reason: reason,
    }),
    success: function (response) {
      if (response) {
        alert("PT 변경에 성공했습니다.");
      } else {
        alert("PT 변경에 실패했습니다.");
      }
    },
    error: function () {
      alert("서버 오류가 발생했습니다.");
    },
  });
}

function cancelEdit(originalAmount) {
  const $ptAmount = $("#ptAmount");

  // 기존 PT 개수로 복원
  $ptAmount.html(originalAmount);

  // 버튼 복원
  $("#PTsaveBtn").replaceWith(
    `<button id="PTeditBtn" class="bg-blue-500 text-white px-4 py-2 rounded" onclick="editPT(${originalAmount})">수정</button>`
  );
  $("#PTcancelBtn").remove();
}

function showMealsInfo(meals) {
  console.log(meals);
  const $mealInfoDiv = $("#diet-card");

  let mealHtml = `
    <div class="row">
      <div class="col-md-4 mb-3">
        <div class="card">
          <div class="card-body">`;

  if (meals.savedFileName) {
    mealHtml += `
      <img src="/uploads/meal/${meals.savedFileName}" class="img-fluid rounded mb-3" alt="식단 이미지">`;
  }

  mealHtml += `
            <h5 class="card-title">마지막 식사</h5>
            <h6 class="card-subtitle mb-2 text-muted">${meals.mealType}</h6>
            <p class="card-text">칼로리: <span>${meals.totalCalories}</span> kcal</p>
            <p class="card-text">탄수화물: <span>${meals.totalCarbs}</span> g</p>
            <p class="card-text">단백질: <span>${meals.totalProtein}</span> g</p>
            <p class="card-text">지방: <span>${meals.totalFat}</span> g</p>
          </div>
        </div>
      </div>
    </div>`;

  $mealInfoDiv.html(mealHtml);
}

function noMealsInfo() {
  const $mealInfoDiv = $("#diet-card");
  $mealInfoDiv.html("해당 이용자의 오늘의 식단"); // HTML을 완전히 비움
}

function showWorkInfo(workout) {
  console.log("워크아웃", workout);
  const $workInfoDiv = $("#workout-card");

  let workHtml = `
    <table class="table table-bordered text-center" id="workoutTable">
      <thead class="bg-gray-800">
        <tr class="text-white">
          <th>부위</th>
          <th>운동 이름</th>
          <th>세트</th>
          <th>횟수</th>
          <th>무게 (KG)</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td>${workout.part}</td>
          <td>${workout.exercise}</td>
          <td>${workout.sets}</td>
          <td>${workout.reps}</td>
          <td>${workout.weight}</td>
        </tr>
      </tbody>
    </table>
  `;

  $workInfoDiv.html(workHtml);
}

function noWorkInfo() {
  const $workInfoDiv = $("#workout-card");
  $workInfoDiv.html("해당 이용자의 오늘의 운동"); // HTML을 완전히 비움
}
