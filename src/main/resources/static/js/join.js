$(document).ready(function () {
    $("#joinForm").submit(function (event) {
        event.preventDefault(); // 폼 기본 제출 막기

        const formData = {
            userId: $("#userId").val(),
            password: $("#password").val(),
            name: $("#name").val(),
            gender: $("input[name='gender']:checked").val(),
            birthdate: $("#birthdate").val(),
            email: $("#email").val(),
            contact: $("#contact").val()
        };

        console.log("보낼 데이터:", formData); // 데이터 확인용 로그

        $.ajax({
            type: "POST",
            url: "/user/joinProc",
            contentType: "application/json",
            data: JSON.stringify(formData),
            success: function (response) {
                alert("회원가입 성공! 로그인 페이지로 이동합니다.");
                window.location.href = "/user/login";
            },
            error: function (xhr) {
                alert("회원가입 실패! 다시 확인해주세요.");
            }
        });
    });
});
