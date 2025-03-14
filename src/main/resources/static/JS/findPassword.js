document.addEventListener("DOMContentLoaded", function () {
    const messageContainer = document.getElementById("messageContainer");

    // ✅ Thymeleaf에서 전달된 메시지 가져오기
    const successMessage = "[[${successMessage}]]";
    const errorMessage = "[[${error}]]";

    // ✅ 메시지 표시
    if (successMessage && successMessage !== "null") {
        messageContainer.innerHTML = `<p class="text-success fw-bold">${successMessage}</p>`;
    } else if (errorMessage && errorMessage !== "null") {
        messageContainer.innerHTML = `<p class="text-danger fw-bold">${errorMessage}</p>`;
    }
});
