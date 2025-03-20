// admin.js

// 트레이너 삭제
function deleteTrainer(userId) {
    if (confirm("정말 삭제하시겠습니까?")) {
        fetch(`/admin/deleteTrainer/${userId}`, { method: "DELETE" })
            .then(response => response.json())
            .then(() => location.reload());
    }
}

// 트레이너 승인
function approveTrainer(userId) {
    fetch(`/admin/approveTrainer/${userId}`, { method: "PUT" })
        .then(response => response.json())
        .then(() => location.reload());
}

// 트레이너 거절
function rejectTrainer(userId) {
    fetch(`/admin/rejectTrainer/${userId}`, { method: "DELETE" })
        .then(response => response.json())
        .then(() => location.reload());
}

// 회원 삭제
function deleteUser(userId) {
    if (confirm("정말 삭제하시겠습니까?")) {
        fetch(`/admin/deleteUser/${userId}`, { method: "DELETE" })
            .then(response => response.json())
            .then(() => location.reload());
    }
}

// 공지사항 업데이트
function updateNotice() {
    const text = document.getElementById("noticeText").value;
    fetch("/admin/updateNotice", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: text
    }).then(response => response.json())
      .then(() => alert("공지사항이 업데이트되었습니다!"));
}
