// ✅ 현재 URL에서 mealDate 가져오기
let urlParams = new URLSearchParams(window.location.search);
let selectedDate = urlParams.get("mealDate") || new Date().toISOString().split("T")[0];
let mealId = document.querySelector(".card .edit-meal-btn")?.getAttribute("data-mealid") || null;



// ✅ 식단 댓글 작성
function submitMealCommentForm(event) {
    event.preventDefault();

    let content = document.getElementById("mealCommentInput").value.trim();
    let mealDate = selectedDate;

    // ✅ 식단 ID 가져오기
    let mealId = document.querySelector(".card .edit-meal-btn")?.getAttribute("data-mealid") || null;

    if (!content) {
        alert("댓글을 입력하세요!");
        return;
    }

    let commentData = {
        content: content,
        createdAt: mealDate + "T12:00:00",
        workoutId: null,
        mealId: mealId  // ✅ 이제 mealId를 저장!
    };

    $.ajax({
        url: "/comments/add",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify(commentData),
        success: function () {
            document.getElementById("mealCommentInput").value = "";
            loadMealComments(mealDate);
        },
        error: function () {
            alert("댓글 추가 실패!");
        }
    });
}


// ✅ 식단 댓글 불러오기
function loadMealComments(date) {

    $.ajax({
        url: `/comments/meal/date/${date}`,
        type: "GET",
        success: function (comments) {

            let commentList = document.getElementById("mealCommentList");
            commentList.innerHTML = "";

            if (comments.length === 0) {
                commentList.innerHTML = "<li>댓글이 없습니다.</li>";
                return;
            }

            comments.forEach(comment => {

                let li = document.createElement("li");
                li.innerHTML = `
                    <strong>${comment.userId || "익명"}</strong>: ${comment.content}
                    <button class="btn btn-sm btn-danger" onclick="deleteMealComment(${comment.commentId})">삭제</button>
                `;
                commentList.appendChild(li);
            });
        },
        error: function (xhr, status, error) {
            console.error("❌ 댓글 불러오기 실패!", error);
        }
    });
}


// ✅ 식단 댓글 삭제
function deleteMealComment(commentId) {
    if (!confirm("정말 삭제하시겠습니까?")) return;

    $.ajax({
        url: `/comments/${commentId}`,
        type: "DELETE",
        success: function () {
            loadMealComments(selectedDate);
        },
        error: function () {
            alert("댓글 삭제 실패!");
        }
    });
}

// ✅ 페이지 로드 시, 선택된 날짜의 댓글 불러오기
document.addEventListener("DOMContentLoaded", function () {
    loadMealComments(selectedDate);
});
