// 회원 정보 데이터
let users = {
  A: { 이름: "김철수", 나이: 25, 성별: "남", 키: "178cm", 체중: "75kg" },
  B: { 이름: "이영희", 나이: 22, 성별: "여", 키: "165cm", 체중: "55kg" },
  C: { 이름: "박민수", 나이: 30, 성별: "남", 키: "182cm", 체중: "85kg" },
  D: { 이름: "정수진", 나이: 27, 성별: "여", 키: "170cm", 체중: "60kg" },
  E: { 이름: "최준호", 나이: 23, 성별: "남", 키: "175cm", 체중: "70kg" },
};

// 회원 목록을 동적으로 생성하는 함수
function updateUserList() {
  let $memberList = $(".memberList");
  $memberList.empty(); // 기존 목록 초기화

  Object.keys(users).forEach((key) => {
    $memberList.append(`
                    <li>
                        <a href="#" class="block bg-blue-500 text-white p-2 rounded-lg text-center hover:bg-blue-700 transition" data-user="${key}">
                            ${key}
                        </a>
                    </li>
                `);
  });

  // 새롭게 추가된 버튼에 클릭 이벤트 바인딩
  $(".memberList a")
    .off("click")
    .on("click", function (event) {
      event.preventDefault();
      let userKey = $(this).data("user");
      showUserInfo(userKey);
    });
}

// 회원 정보 표시 함수
function updateUserList() {
  let $memberList = $(".memberList");
  $memberList.empty(); // 기존 목록 초기화

  Object.keys(users).forEach((key) => {
    $memberList.append(`
                    <li>
                        <a href="#" class="block bg-blue-500 text-white p-2 rounded-lg text-center hover:bg-blue-700 transition" data-user="${key}">
                            ${key}
                        </a>
                    </li>
                `);
  });

  // 새롭게 추가된 버튼에 클릭 이벤트 바인딩
  $(".memberList a")
    .off("click")
    .on("click", function (event) {
      event.preventDefault();
      let userKey = $(this).data("user");
      showUserInfo(userKey);
    });
}

// 회원 정보 표시 및 수정 기능 추가
function showUserInfo(userKey) {
  const user = users[userKey];
  const $userInfoDiv = $("#userInfo");

  if (!user) {
    $userInfoDiv.html(
      `<p class="text-red-500">해당 회원이 존재하지 않습니다.</p>`
    );
    return;
  }

  $userInfoDiv.html(`
                <h3 class="text-2xl font-semibold text-center text-blue-700">${user.이름}</h3>
                <table class="w-full border border-white mt-2">
                    <tr class="bg-blue-500">
                        <th class="p-2 text-white">나이</th><th class="p-2 text-white">성별</th>
                    </tr>
                    <tr class="border-t">
                        <td class="p-2"><input type="number" id="editAge" class="border p-1 w-full text-center" value="${user.나이}" readonly></td>
                        <td class="p-2"><input type="text" id="editGender" class="border p-1 w-full text-center" value="${user.성별}" readonly></td>
                    </tr>
                    <tr class="bg-blue-500">
                        <th class="p-2 text-white">키</th><th class="p-2 text-white">체중</th>
                    </tr>
                    <tr class="border-t">
                        <td class="p-2"><input type="text" id="editHeight" class="border p-1 w-full text-center" value="${user.키}" readonly></td>
                        <td class="p-2"><input type="text" id="editWeight" class="border p-1 w-full text-center" value="${user.체중}" readonly></td>
                    </tr>
                </table>
                <div class="flex justify-end mt-3">
                    <button id="editUserBtn" class="bg-blue-500 text-white px-4 py-2 mt-3 rounded hover:bg-blue-700 transition">수정</button>
                    <button id="saveUserBtn" class="bg-green-500 text-white px-4 py-2 mt-3 rounded hover:bg-green-700 transition hidden">저장</button>
                </div>
            `);

  // 수정 버튼 클릭 시 입력 필드 활성화
  $("#editUserBtn")
    .off("click")
    .on("click", function () {
      $("#editAge, #editGender, #editHeight, #editWeight").removeAttr(
        "readonly"
      );
      $("#editUserBtn").hide();
      $("#saveUserBtn").show();
    });

  // 저장 버튼 클릭 시 데이터 업데이트 및 다시 읽기 전용으로 변경
  $("#saveUserBtn")
    .off("click")
    .on("click", function () {
      users[userKey].나이 = $("#editAge").val();
      users[userKey].성별 = $("#editGender").val();
      users[userKey].키 = $("#editHeight").val();
      users[userKey].체중 = $("#editWeight").val();

      alert("회원 정보가 수정되었습니다!");
      showUserInfo(userKey); // 변경 사항을 다시 표시
    });
}

// 페이지가 로드될 때 회원 목록 자동 생성
$(document).ready(function () {
  updateUserList();

  // 버튼 클릭 이벤트 바인딩
  $("#addUserBtn").on("click", addUser);
  $("#removeUserBtn").on("click", removeUser);
});
