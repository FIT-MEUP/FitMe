document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.getElementById("loginForm");

    loginForm.addEventListener("submit", (event) => {
        event.preventDefault(); // 기본 폼 제출 막기

        // 아이디와 비밀번호 값 가져오기
        const userId = document.getElementById("userId").value;
        const password = document.getElementById("password").value;

        console.log("입력한 아이디:", userId);
        console.log("입력한 비밀번호:", password);

        // 여기서 실제 로그인 로직을 구현하면 됨 (Ajax, fetch 등)
        // 예: fetch("/user/login", { method: "POST", ... })

        alert("로그인 버튼 클릭됨! (백엔드 미연동 상태)");
    });
});
