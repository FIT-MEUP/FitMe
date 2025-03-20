// 드롭다운 메뉴 토글
function toggleDropdown() {
  var dropdown = document.getElementById("dropdownMenu");
  dropdown.classList.toggle("hidden");
}

// 클릭 외부 감지 시 드롭다운 닫기
document.addEventListener("click", function(event) {
  var dropdown = document.getElementById("dropdownMenu");
  var button = document.getElementById("userMenu");
  if (!button.contains(event.target) && !dropdown.contains(event.target)) {
    dropdown.classList.add("hidden");
  }
});

// 명언 API 호출 및 출력
function loadMotivationalQuote() {
  fetch('/api/quote')
    .then(response => response.json())
    .then(data => {
      document.getElementById("motivational-quote").innerText = data.quote;
    })
    .catch(error => {
      console.error("명언 호출 중 오류:", error);
      document.getElementById("motivational-quote").innerText = "명언을 불러오는 중 오류 발생.";
    });
}

// 트레이너 목록으로 스크롤
function scrollToTrainers() {
  document.getElementById("trainer-list").scrollIntoView({ behavior: "smooth" });
}

window.onload = loadMotivationalQuote;



document.addEventListener("DOMContentLoaded", function () {
    let profileCard = document.querySelector(".profile-card");
    let container = document.querySelector(".gaduriContainer");

    let offsetTop = profileCard.offsetTop; // 초기 프로필 카드 위치
    let containerBottom = container.offsetTop + container.offsetHeight; // 컨테이너 바닥 위치
    let marginOffset = 20; // 여백

    // ✅ 초기 right 값 설정 (컨테이너 기준)
    function updateInitialRightPosition() {
        
        let rightOffset = window.innerWidth; 
        profileCard.style.right = rightOffset*15/100 + "px";
    }

    updateInitialRightPosition(); // 페이지 로드 시 한 번 실행
    window.addEventListener("resize", updateInitialRightPosition); // 창 크기 변경 시 업데이트

    window.addEventListener("scroll", function () {
        let scrollY = window.scrollY;
        let newTop = scrollY + 100; // 화면 상단에서 100px 간격 유지

        // ✅ 컨테이너 범위를 넘지 않도록 제한
        let maxTop = containerBottom - profileCard.offsetHeight - marginOffset;

        if (scrollY > offsetTop - 100 && newTop < maxTop) {
            profileCard.style.position = "fixed";
            profileCard.style.top = "100px"; // 스크롤 시 상단 고정
        } else if (newTop >= maxTop) {
            profileCard.style.position = "absolute";
            profileCard.style.top = maxTop + "px"; // ✅ 컨테이너 하단 넘어가지 않도록 설정
        } else {
            profileCard.style.position = "static"; // 기본 위치 유지
        }
    });
});
