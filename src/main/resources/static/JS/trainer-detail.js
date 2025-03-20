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


