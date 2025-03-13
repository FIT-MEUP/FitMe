// 회원 정보 데이터
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
        location.reload(); // 페이지 새로고침
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
        location.reload(); // 페이지 새로고침
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

    $.ajax({
      url: `/trainer/select?applicationId=${applicationId}`,
      type: "GET",
      success: function (response) {
        alert("불러왔습니다");
        console.log(response);
        showUserInfo(response)
      },
      error: function (xhr) {
        alert("아앙대");
        console.error("Error:", xhr);
      },
    });

    
  });

  // 공지사항 수정
  let originalNotice = $("#noticeText").text(); // 원래 공지사항 내용 저장

  // 수정 버튼 클릭 시
  $("#editBtn").click(function () {
      $("#editNotice").val(originalNotice).removeClass("hidden"); // 기존 내용 불러오기
      $("#noticeText").addClass("hidden"); // 기존 텍스트 숨기기

      $("#editBtn").addClass("hidden"); // 수정 버튼 숨김
      $("#saveBtn, #cancelBtn").removeClass("hidden"); // 완료, 취소 버튼 표시
  });

  // 취소 버튼 클릭 시
  $("#cancelBtn").click(function () {
      $("#editNotice").addClass("hidden"); // 수정창 숨김
      $("#noticeText").removeClass("hidden"); // 기존 텍스트 표시

      $("#editBtn").removeClass("hidden"); // 수정 버튼 다시 표시
      $("#saveBtn, #cancelBtn").addClass("hidden"); // 완료, 취소 버튼 숨김
  });

  // 완료 버튼 클릭 시 (서버에 데이터 전송)
  $("#saveBtn").click(function () {
      let updatedNotice = $("#editNotice").val(); // 수정된 공지사항 내용

      $.ajax({
          url: "/api/update-notice", // 공지사항 업데이트 API (Spring Boot)
          type: "POST",
          contentType: "application/json",
          data: JSON.stringify({ notice: updatedNotice }),
          success: function (response) {
              alert("공지사항이 업데이트되었습니다.");

              // UI 업데이트
              $("#noticeText").text(updatedNotice).removeClass("hidden"); // 새 내용 적용
              $("#editNotice").addClass("hidden"); // 수정창 숨김

              $("#editBtn").removeClass("hidden"); // 수정 버튼 다시 표시
              $("#saveBtn, #cancelBtn").addClass("hidden"); // 완료, 취소 버튼 숨김
          },
          error: function (xhr) {
              alert("공지사항 수정 중 오류가 발생했습니다.");
              console.error("Error:", xhr);
          }
      });
  });

});


function updateNotice() {
  const text = document.getElementById("noticeText").value;
  fetch("/trainer/updateNotice", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ notice: text })
  }).then(response => response.json())
    .then(() => alert("공지사항이 업데이트되었습니다!"));
}


/*********************** 회원 데이터 표시 ************************/
// 회원 정보 표시 및 수정 기능 추가
function showUserInfo(response) {
  const $userInfoDiv = $("#userInfo");
  console.log(response);
  

  $userInfoDiv.html(`
                <h3 class="text-2xl font-semibold text-left px-2 py-1 text-black"> ${response}</h3>
            `);

 
}

// {
//  /*********************** 회원 데이터터 수정 ************************/
//   // 수정 버튼 클릭 시 입력 필드 활성화
//   $("#editUserBtn")
//     .off("click")
//     .on("click", function () {
//       $("#editAge, #editGender, #editHeight, #editWeight").removeAttr(
//         "readonly"
//       );
//       $("#editUserBtn").hide();
//       $("#saveUserBtn").show();
//     });

//   // 저장 버튼 클릭 시 데이터 업데이트 및 다시 읽기 전용으로 변경
//   $("#saveUserBtn")
//     .off("click")
//     .on("click", function () {
//       users[userKey].나이 = $("#editAge").val();
//       users[userKey].성별 = $("#editGender").val();
//       users[userKey].키 = $("#editHeight").val();
//       users[userKey].체중 = $("#editWeight").val();

//       alert("회원 정보가 수정되었습니다!");
//       showUserInfo(userKey); // 변경 사항을 다시 표시
//     });

//     $userInfoDiv.html(`
//       <h3 class="text-2xl font-semibold text-left px-2 py-1 text-black" th:text="${ApprovedList.Name}"></h3>
//       <table class="w-full border border-white mt-2">
//           <tr class="bg-gray-800">
//               <th class="p-2 text-white">나이</th><th class="p-2 text-white">성별</th>
//           </tr>
//           <tr class="border-t">
//               <td class="p-2"><input type="number" id="editAge" class="border p-1 w-full text-center" value="${ApprovedList.나이}" readonly></td>
//               <td class="p-2"><input type="text" id="editGender" class="border p-1 w-full text-center" value="${ApprovedList.성별}" readonly></td>
//           </tr>
//           <tr class="bg-gray-800">
//               <th class="p-2 text-white">키</th><th class="p-2 text-white">체중</th>
//           </tr>
//           <tr class="border-t">
//               <td class="p-2"><input type="text" id="editHeight" class="border p-1 w-full text-center" value="${user.키}" readonly></td>
//               <td class="p-2"><input type="text" id="editWeight" class="border p-1 w-full text-center" value="${user.체중}" readonly></td>
//           </tr>
//       </table>
//       <div class="flex justify-end mt-3">
//           <button id="editUserBtn" class="bg-blue-500 text-white px-4 py-2 mt-3 rounded hover:bg-blue-700 transition">수정</button>
//           <button id="saveUserBtn" class="bg-green-500 text-white px-4 py-2 mt-3 rounded hover:bg-green-700 transition hidden">저장</button>
//       </div>
//   `);

// }