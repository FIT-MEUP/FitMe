document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.getElementById("loginForm");
    
    // ✅ 성공 메시지 확인 후 alert 띄우기
    const successMessage = document.getElementById("successMessage")?.value;
    if (successMessage && successMessage.trim() !== "") {
        alert(successMessage);
    }

    // ✅ 로그인 실패 시 alert 띄우기
    const errorMessage = document.getElementById("errorMessage")?.value;
    if (errorMessage && errorMessage.trim() !== "") {
        alert("아이디나 비밀번호를 잘못 입력하셨습니다.");
    }

    // ✅ 로그인 폼 유효성 검사 (Ajax 연동 가능)
    loginForm.addEventListener("submit", (event) => {
        const userEmail = document.getElementById("userEmail").value;
        const password = document.getElementById("password").value;

        if (!userEmail || !password) {
            alert("아이디와 비밀번호를 입력해주세요!");
            event.preventDefault(); // 제출 방지
            return;
        }

        console.log("입력한 아이디:", userEmail);
        console.log("입력한 비밀번호:", password);

        // ❗ 여기에 Ajax 연동을 할 경우, fetch("/user/loginProc", { method: "POST", ... }) 로직 추가 가능
    });
});
