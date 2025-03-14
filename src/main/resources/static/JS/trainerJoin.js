document.addEventListener("DOMContentLoaded", function () {
    // 📌 에러 메시지 alert() 표시
    const errorMessage = document.querySelector("meta[name='errorMessage']")?.content;
    if (errorMessage) {
        alert(errorMessage);
    }

    // ✅ 비밀번호 유효성 검사
    document.getElementById("password").addEventListener("input", function () {
        validatePassword();
    });

    // ✅ 이름 유효성 검사 (한글만 입력 가능)
    document.getElementById("userName").addEventListener("input", function () {
        validateName();
    });

    // ✅ 연락처 유효성 검사 (010-1234-5678 형식)
    document.getElementById("userContact").addEventListener("input", function () {
        validateContact();
    });

    // ✅ 폼 제출 전 유효성 검사 실행
    document.querySelector("form").addEventListener("submit", function (event) {
        if (!validateForm()) {
            event.preventDefault(); // 🚫 유효성 검사 실패 시 폼 제출 막기
        }
    });
});

// 📌 비밀번호 유효성 검사 함수
function validatePassword() {
    let password = document.getElementById("password").value;
    let passwordError = document.getElementById("passwordError");
    let passwordPattern = /^(?=.*[!@#$%^&*(),.?":{}|<>])[A-Za-z\d!@#$%^&*(),.?":{}|<>]{6,}$/;

    if (!passwordPattern.test(password)) {
        passwordError.innerText = "비밀번호는 6자 이상이며 특수문자를 포함해야 합니다.";
        return false;
    } else {
        passwordError.innerText = "";
        return true;
    }
}

// 📌 이름 유효성 검사 함수
function validateName() {
    let name = document.getElementById("userName").value;
    let nameError = document.getElementById("nameError");
    let namePattern = /^[가-힣]+$/;

    if (!namePattern.test(name)) {
        nameError.innerText = "이름은 한글만 입력할 수 있습니다.";
        return false;
    } else {
        nameError.innerText = "";
        return true;
    }
}

// 📌 연락처 유효성 검사 함수
function validateContact() {
    let contact = document.getElementById("userContact").value.replace(/[^0-9-]/g, ""); // 숫자와 '-'만 허용
    let contactError = document.getElementById("contactError");
    let contactPattern = /^\d{3}-\d{4}-\d{4}$/;

    document.getElementById("userContact").value = contact; // 숫자 및 '-' 유지

    if (!contactPattern.test(contact)) {
        contactError.innerText = "연락처 형식은 010-xxxx-xxxx 이어야 합니다.";
        return false;
    } else {
        contactError.innerText = "";
        return true;
    }
}

// 📌 폼 전체 유효성 검사 함수
function validateForm() {
    let isPasswordValid = validatePassword();
    let isNameValid = validateName();
    let isContactValid = validateContact();

    if (!isPasswordValid || !isNameValid || !isContactValid) {
        alert("입력값을 확인하세요.");
        return false;
    }
    return true;
}
