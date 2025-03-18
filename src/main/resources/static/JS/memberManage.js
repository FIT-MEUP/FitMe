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

      $.ajax({
        url: "/trainer/saveAnnouncement",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({ announcement: updatedNotice }),
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
        },
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
        },
      });
    });

    //------------------------------------------------------
    // (3) 회원 목록 클릭 -> 대화 로드
    //------------------------------------------------------
    $(".select-btn").click(function () {
      let applicationId = $(this).data("id");
      let userId = $(this).data("user-id");

      // 식단 버튼과 운동 버튼 보이기
      $("#diet-section button")
        .removeClass("hidden")
        .off("click")
        .on("click", function () {
          window.location.href = `/meals?userId=${userId}`;
        });

      $("#workout-section button")
        .removeClass("hidden")
        .off("click")
        .on("click", function () {
          window.location.href = `/work?userId=${userId}`;
        });

      $("#userInfo button")
        .removeClass("hidden")
        .off("click")
        .on("click", function () {
          window.location.href = `/mypage?userId=${userId}`;
        });

      updateUnreadCountToZero(userId);
      console.log("선택한 applicationId:", applicationId);

      // (A) 회원 정보 조회
      $.ajax({
        url: `/trainer/userPreview?userId=${userId}`,
        type: "GET",
        success: function (response) {
          showUserInfo(response, userId);
        },
        error: function (xhr) {
          noUserInfo();
          console.error("Error:", xhr);
        },
      });

      // 회원 PT 조회 (선택된 신청서의 정보를 출력)
      $.ajax({
        url: `/trainer/selectPT?userId=${userId}`,
        type: "GET",
        success: function (response) {
          showPTInfo(response, userId);
        },
        error: function (xhr) {
          alert("PT 정보 조회 중 오류가 발생했습니다.");
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

      // (B) 채팅 프래그먼트 로드
      $.ajax({
        url: `/chat?applicationId=${encodeURIComponent(applicationId)}`,
        type: "GET",
        success: function (htmlFragment) {
          $("#chatFragmentContainer").html(htmlFragment);
          $("#chatFragmentContainer").show();

          if (typeof initChat === "function") {
            initChat();
          }

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
            console.log("로그인(트레이너):", window.currentUser);
            console.log("대상 회원:", window.targetUser);
          }

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
        content: message,
      };
      if (window.stompClient && window.stompClient.connected) {
        window.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
      } else {
        console.error("WS 연결이 안 되어있습니다.");
      }
      msgInput.val("");
    };

    //------------------------------------------------------
    // (5) 채팅용 WebSocket 연결 함수
    //------------------------------------------------------
    function connectChat() {
      const socket = new SockJS("/ws");
      window.stompClient = Stomp.over(socket);

      window.stompClient.connect({}, function (frame) {
        console.log('채팅용 WS 연결 성공: ' + frame);
        $("#sendBtn").prop("disabled", false);
        window.stompClient.subscribe(`/queue/chat/${window.currentUser.userId}`, function (response) {
          const chat = JSON.parse(response.body);
          if (
            (chat.senderId === window.targetUser.userId && chat.receiverId === window.currentUser.userId) ||
            (chat.senderId === window.currentUser.userId && chat.receiverId === window.targetUser.userId)
          ) {
            updateChatWindow(chat);
            console.log("채팅 메시지 수신:", chat);
          } else {
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

      if (chat.fileUrl && chat.fileType) {
        let filePreview = "";
        switch (chat.fileType) {
          case "image":
            filePreview = `<img src="${chat.fileUrl}" alt="이미지" style="max-width:200px;">`;
            break;
          case "video":
            filePreview = `<video controls width="300"><source src="${chat.fileUrl}" type="video/mp4"/>영상 불가</video>`;
            break;
          case "audio":
            filePreview = `<audio controls><source src="${chat.fileUrl}" type="audio/mpeg"/>오디오 불가</audio>`;
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
    $(document).on("click", "#fileSelectIcon", function() {
      $("#fileInput").click();
    });

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



// 보여질 정보들
function showPTInfo(response, userId) {
  const $userInfoDiv = $("#userPTInfo");

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
  $ptAmount.html(originalAmount);

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
          <div class="card-body">
  `;

  if (meals.savedFileName) {
    mealHtml += `
      <img src="/uploads/meal/${meals.savedFileName}" class="img-fluid rounded mb-3" alt="식단 이미지">
    `;
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
    </div>
  `;

  $mealInfoDiv.html(mealHtml);
}

function noMealsInfo() {
  const $mealInfoDiv = $("#diet-card");
  $mealInfoDiv.html("해당 이용자의 오늘의 식단");
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
  $workInfoDiv.html("해당 이용자의 오늘의 운동");
}

function showUserInfo(latestData) {
  console.log("latestData", latestData);
  const $workInfoDiv = $("#user-card");

  let workHtml = `
    <table class="table table-bordered text-center" id="workoutTable">
      <thead class="bg-gray-800">
        <tr class="text-white">
          <th>키</th>
          <th>체중</th>
          <th>BMI</th>
          <th>체지방률</th>
          <th>골격근</th>
          <th>기초대사량</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td id="height" th:text="${latestData.height} + ' cm'">${latestData.height} cm</td>
          <td id="weight" th:text="${latestData.weight} + ' kg'">${latestData.weight} kg</td>
          <td id="bmi" th:text="${latestData.bmi}">${latestData.bmi}</td>
          <td id="fatMass" th:text="${latestData.fatMass} + '%'">${latestData.fatMass}%</td>
          <td id="muscleMass" th:text="${latestData.muscleMass} + 'kg'">${latestData.muscleMass}</td>
          <td id="basalMetabolicRate" th:text="${latestData.basalMetabolicRate} + ' kcal'">${latestData.basalMetabolicRate} kcal</td>
        </tr>
      </tbody>
    </table>
  `;

  $workInfoDiv.html(workHtml);
}

function noUserInfo() {
  const $workInfoDiv = $("#user-card");
  $workInfoDiv.html("해당 이용자의 최신 신체 정보");
}
}
