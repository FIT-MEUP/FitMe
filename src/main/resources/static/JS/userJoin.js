document.addEventListener("DOMContentLoaded", function () {
    // 비밀번호 유효성 검사
    document.getElementById("password").addEventListener("input", function () {
        let password = this.value;
        let passwordError = document.getElementById("passwordError");
        let passwordPattern = /^(?=.*[!@#$%^&*(),.?":{}|<>])[A-Za-z\d!@#$%^&*(),.?":{}|<>]{6,}$/;

        if (!passwordPattern.test(password)) {
            passwordError.innerText = "비밀번호는 6자 이상이며 특수문자를 포함해야 합니다.";
        } else {
            passwordError.innerText = "";
        }
    });

    // 이름 유효성 검사 (한글만 입력 가능)
    document.getElementById("userName").addEventListener("input", function () {
        let name = this.value;
        let nameError = document.getElementById("nameError");
        let namePattern = /^[가-힣]+$/;

        if (!namePattern.test(name)) {
            nameError.innerText = "이름은 한글만 입력할 수 있습니다.";
        } else {
            nameError.innerText = "";
        }
    });

    // 연락처 유효성 검사 (010-1234-5678 형식)
    document.getElementById("userContact").addEventListener("input", function () {
        let contact = this.value.replace(/[^0-9-]/g, ""); // 숫자와 '-'만 허용
        let contactError = document.getElementById("contactError");
        let contactPattern = /^\d{3}-\d{4}-\d{4}$/;

        this.value = contact;

        if (!contactPattern.test(contact)) {
            contactError.innerText = "연락처 형식은 010-xxx-xxxx 이어야 합니다.";
        } else {
            contactError.innerText = "";
        }
    });
});

// 폼 제출 전 유효성 검사
function validateForm() {
    let passwordError = document.getElementById("passwordError").innerText;
    let nameError = document.getElementById("nameError").innerText;
    let contactError = document.getElementById("contactError").innerText;

    if (passwordError || nameError || contactError) {
        alert("입력값을 확인하세요.");
        return false;
    }
    return true;
}
