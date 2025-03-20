$(document).ready(function () {
    //------------------------------------------------------
    // (1) 트레이너 알림용 WebSocket 연결
    //------------------------------------------------------
    function connectTrainerNotificationWebSocket() {
        var socket = new SockJS("/ws");
        var notificationClient = Stomp.over(socket);
        notificationClient.connect(
            {},
            function (frame) {
                console.log("Trainer 알림용 WS 연결 성공:", frame);
                // 최상위 .container에 저장된 trainer의 userId 사용
                var trainerUserId = parseInt($("#trainerInfo").data("user-name"));
                console.log("트레이너 userId:", trainerUserId);
                notificationClient.subscribe(
                    `/queue/notifications/${trainerUserId}`,
                    function (response) {
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
                    }
                );

            }, function (error) {
                console.error("Trainer 알림용 WS 연결 오류:", error);
            });

    }


    function removeOnlineDot(userId) {
        var $selectBtn = $(`.select-btn[data-user-id="${userId}"]`);
        if ($selectBtn.length) {
            $selectBtn.find(".greenDot").remove();
        }
    }


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
            if (!response || Object.keys(response).length === 0) {
                noUserInfo();
            } else {
                showUserInfo(response, userId);
            }
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
            console.log("Response:", response);
            if (!response || Object.keys(response).length === 0) {
                noMealsInfo();
            } else {
                showMealsInfo(response);
            }
        },
        error: function (xhr) {
            noMealsInfo();
        },
    });

    $.ajax({
        url: `/trainer/workPreview?userId=${userId}`,
        type: "GET",
        success: function (response) {
            if (!response || Object.keys(response).length === 0) {
                noWorkInfo();
            } else {
                showWorkInfo(response);
            }
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


        // 채팅창이 렌더링되고 나서 스크롤 이동
            setTimeout(function() {
                const conversationArea = document.getElementById("conversationArea");
                if (conversationArea) {
                    conversationArea.scrollTop = conversationArea.scrollHeight;
                }
            }, 1);

            conversationArea.scrollTop = conversationArea.scrollHeight;

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
        window.stompClient.send(
            "/app/chat.sendMessage",
            {},
            JSON.stringify(chatMessage)
        );
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

    window.stompClient.connect(
        {},
        function (frame) {
            console.log("채팅용 WS 연결 성공: " + frame);
            $("#sendBtn").prop("disabled", false);
            window.stompClient.subscribe(
                `/queue/chat/${window.currentUser.userId}`,
                function (response) {
                    const chat = JSON.parse(response.body);
                    if (
                        (chat.senderId === window.targetUser.userId &&
                            chat.receiverId === window.currentUser.userId) ||
                        (chat.senderId === window.currentUser.userId &&
                            chat.receiverId === window.targetUser.userId)
                    ) {
                        updateChatWindow(chat);
                        console.log("채팅 메시지 수신:", chat);
                    } else {
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

//------------------------------------------------------
// (6) 채팅창 UI 업데이트 함수 (파일/텍스트 공용)
//------------------------------------------------------

    function updateChatWindow(chat) {
        const conversationArea = document.getElementById("conversationArea");

        // 발신자에 따라 전체 컨테이너의 정렬 지정
        const messageContainer = document.createElement("div");
        if (chat.senderId === window.currentUser.userId) {
            messageContainer.className = "py-1 ";
        } else {
            messageContainer.className = "py-1";
        }

        // 말풍선 스타일을 적용할 내부 div 생성
        const messageBubble = document.createElement("div");
        if (chat.senderId === window.currentUser.userId) {
            messageBubble.className =
                "bg-green-400 text-white rounded-lg p-2 max-w-[150px] break-words whitespace-normal rightbox ml-auto";
            // 발신자 이름 추가
            const messageSender = document.createElement("strong");
            messageSender.textContent = window.currentUser.userName;
            messageSender.className = "flex flex-col text-right";
            messageContainer.appendChild(messageSender);
        } else {
            messageBubble.className =
                "bg-white text-gray-900 rounded-lg p-2 max-w-[150px] border break-words whitespace-normal leftbox";
            // 수신자 이름 추가
            const messageSender = document.createElement("strong");
            messageSender.textContent = window.targetUser.userName;
            messageSender.className = "flex flex-col text-left";

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
            downloadLink.textContent = "다운로드";
            downloadLink.style.display = "block";
            downloadLink.style.marginTop = "5px";
            downloadLink.style.fontSize = "12px";
            if (chat.senderId === window.currentUser.userId) {
                downloadLink.style.color = "white";
            } else {
                downloadLink.style.color = "gray";
            }

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

// //------------------------------------------------------
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
// (9) 파일 업로드 (plus.png + #fileInput)
//------------------------------------------------------
$(document).on("click", "#fileSelectIcon", function () {
    $("#fileInput").click();
});

$(document).on("change", "#fileInput", function (e) {
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
        body: formData,
    })
        .then((resp) => resp.json())
        .then((result) => {
            const chatMessage = {
                senderId: window.currentUser.userId,
                receiverId: window.targetUser.userId,
                content: "",
                originalFileName: result.originalFileName,
                savedFileName: result.savedFileName,
                fileType: result.fileType,
                fileUrl: result.fileUrl,
            };
            if (window.stompClient && window.stompClient.connected) {
                window.stompClient.send(
                    "/app/chat.sendMessage",
                    {},
                    JSON.stringify(chatMessage)
                );
            } else {
                console.error("WS 연결이 안 되어있습니다.");
            }
        })
        .catch((err) => {
            console.error("파일 업로드 실패:", err);
        });
    }
});


// 보여질 정보들
function showPTInfo(response, userId) {
    const $userInfoDiv = $("#userPTInfo");

    $userInfoDiv.html(`
      <table id="userPTInfo" class="table table-bordered text-center w-full h-16">
        <tr class="bg-gray-800">
            <td><h3 class="text-lg font-semibold col-span-1  text-white">PT 횟수</h3></td>
        </tr>
        <tr class="max-w-full overflow-hidden">
          <td>
            <div class="border text-center" id="ptAmount">${response.changeAmount} 회 </div>
          </td>
        </tr>
      </table>
      <div class="text-right w-full">
        <button id="PTeditBtn" class="bg-blue-500 text-white px-4 rounded h-8" onclick="editPT(${response.changeAmount}, ${userId})">수정</button>
      </div>
    `);
}

function editPT(currentAmount, userId) {
    const $ptAmount = $("#ptAmount");
    const $PTeditBtn = $("#PTeditBtn");

    $ptAmount.html(`
      <tr class="flex gap-2 w-full max-w-full mt-1">
        <td class="w-1/4">
          <input type="number" id="newPTAmount" class="border px-2 py-1 w-full max-w-40 text-center" value="${currentAmount}">
        </td>
        <td class="w-3/4">
          <input type="text" id="reason" class="border px-2 py-1 w-full max-w-60" placeholder="변경 사유 입력">
        </td>
      </tr>
    `);

    $PTeditBtn.replaceWith(`
      <section class="flex justify-center items-center w-full gap-2 mt-1 overflow-hidden">
        <button id="PTsaveBtn" class="w-32 bg-green-500 text-white px-4 rounded h-8" onclick="savePT(${userId})">
          확인
        </button>
        <button id="PTcancelBtn" class="w-32 bg-red-500 text-white px-4 rounded h-8" onclick="cancelEdit(${currentAmount})">
          취소
        </button>
      </section>
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
                // 변경된 값을 화면에 반영
                const $ptAmount = $("#ptAmount");
                $ptAmount.html(newPTAmount + " 회");

                // "확인" 버튼을 다시 "수정" 버튼으로 변경
                $("#PTsaveBtn").replaceWith(
                    `
            <div class="text-right w-full">
              <button id="PTeditBtn" class="bg-blue-500 text-white px-4 rounded h-8" onclick="editPT(${newPTAmount}, ${userId})">수정</button>
            </div>
            `
                );

                // "취소" 버튼 제거
                $("#PTcancelBtn").remove();
            } else {
            }
        },
        error: function () { },
    });
}

function cancelEdit(originalAmount) {
    const $ptAmount = $("#ptAmount");
    $ptAmount.html(originalAmount + " 회");

    $("#PTsaveBtn").replaceWith(
        `
      <div class="text-right w-full">
      <button id="PTeditBtn" class="bg-blue-500 text-white px-4 rounded h-8" onclick="editPT(${originalAmount})">수정</button>
      </div>
      `
    );
    $("#PTcancelBtn").remove();
}

function showMealsInfo(meals) {
    console.log(meals);
    const $mealInfoDiv = $("#diet-card");

    let mealHtml = `
      <table>`;

    // `HTML` 배열을 반복하여 테이블 행 생성
    meals.forEach(meals => {
        mealHtml += `
      <td>
      <div class="row text-left">
        <div class="mb-4">
          <div class="card shadow-sm border-0 p-4">
            <div class="card-body">
    `;

        if (meals.savedFileName) {
            mealHtml += `
          <img src="/uploads/meal/${meals.savedFileName}" class=" w-44 h-44 rounded mb-3 shadow-sm border-black border-2 object-cover" alt="식단 이미지">
        `;
        }

        mealHtml += `
                <h6 class="card-subtitle mb-2 text-muted">${meals.mealType}</h6>
                <p class="card-text">🔥 칼로리: <span>${meals.totalCalories}</span> kcal</p>
                <p class="card-text">🍞 탄수화물: <span>${meals.totalCarbs}</span> g</p>
                <p class="card-text">🍗 단백질: <span>${meals.totalProtein}</span> g</p>
                <p class="card-text">🥑 지방: <span>${meals.totalFat}</span> g</p>
              </div>
            </div>
          </div>
        </div>
      </td>
      `;
    });
    mealHtml += `</table>`;

    $mealInfoDiv.html(mealHtml);
}

function noMealsInfo() {
    const $mealInfoDiv = $("#diet-card");
    $mealInfoDiv.html("해당 이용자의 오늘의 식단이 없습니다");
}

function showWorkInfo(workout) {
    console.log("워크아웃", workout);
    const $workInfoDiv = $("#workout-card");

    let workHtml = `
      <table class="table table-bordered text-center" id="workoutTable">
        <thead class="bg-gray-800">
          <tr class="text-white p-2">
            <th class="p-2">부위</th>
            <th class="p-2">운동 이름</th>
            <th class="p-2">세트</th>
            <th class="p-2">횟수</th>
            <th class="p-2">무게 (KG)</th>
          </tr>
        </thead>
        <tbody>
    `;

    // `workout` 배열을 반복하여 테이블 행 생성
    workout.forEach(workout => {
        workHtml += `
        <tr>
          <td>${workout.part}</td>
          <td>${workout.exercise}</td>
          <td>${workout.sets}</td>
          <td>${workout.reps}</td>
          <td>${workout.weight}</td>
        </tr>
      `;
    });

    // 테이블 닫기
    workHtml += `</tbody></table>`;

    // HTML 삽입

    $workInfoDiv.html(workHtml);
}

function noWorkInfo() {
    const $workInfoDiv = $("#workout-card");
    $workInfoDiv.html("해당 이용자의 오늘의 운동이 없습니다");
}

function showUserInfo(latestData) {
    console.log("latestData", latestData);
    const $workInfoDiv = $("#user-card");

    let workHtml = `
      <table class="table table-bordered text-center h-16" id="workoutTable ">
        <thead class="bg-gray-800">
          <tr class="text-white h-8">
            <th>키</th>
            <th>체중</th>
            <th>BMI</th>
            <th class="text-[12px]" >체지방률</th>
            <th class="text-[12px]" >골격근</th>
            <th class="text-[12px]" >기초대사량</th>
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
    $workInfoDiv.html("해당 이용자의 최신 신체 정보가 없습니다");
}

$(document).on("click", ".select-btn", function (event) {
    event.preventDefault(); // 기본 동작 방지

    // 클릭된 버튼에서 회원 이름 가져오기
    let userName = $(this).find(".username").text();
    console.log("클릭된 회원 이름:", userName);

    // "회원 정보" 제목 변경
    $("#userinfoname").text(userName);
});
