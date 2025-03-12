document.addEventListener("DOMContentLoaded", () => {
    const changePasswordForm = document.querySelector("form");
    const successMessage = document.getElementById("successMessage")?.value;

    // ✅ 성공 메시지 확인 후 alert 표시 & 로그인 페이지로 이동
    if (successMessage && successMessage.trim() !== "") {
        alert(successMessage);
        window.location.href = "/user/login";
    }

    // ✅ 비밀번호 유효성 검사 & 폼 제출 방지
    changePasswordForm.addEventListener("submit", (event) => {
        let newPassword = document.getElementById("newPassword").value;
        let confirmPassword = document.getElementById("confirmPassword").value;

        if (newPassword !== confirmPassword) {
            alert("새 비밀번호가 일치하지 않습니다!");
            event.preventDefault();
            return;
        }

        let passwordRegex = /^(?=.*[!@#$%^&*(),.?\":{}|<>]).{6,}$/;
        if (!passwordRegex.test(newPassword)) {
            alert("비밀번호는 6자 이상이며 특수문자를 포함해야 합니다.");
            event.preventDefault();
            return;
        }
    });
});
