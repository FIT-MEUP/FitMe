let loggedInUserId = null;
let selectedUserId = null;
let selectedDate = null;
let role = null;
let selectedMealId = null; // ✅ 전역 mealId 저장용 추가

document.addEventListener("DOMContentLoaded", function () {
    loggedInUserId = document.getElementById("loggedInUserId")?.value || null;
    let urlParams = new URLSearchParams(window.location.search);
    selectedDate = urlParams.get("mealDate") || new Date().toISOString().split("T")[0];
    selectedUserId = urlParams.get("userId") || loggedInUserId || null;
    role = document.getElementById("role")?.value || null;

    // ✅ 첫 번째 식단 카드에서 mealId 가져오기
    let mealIdElement = document.querySelector(".card .edit-meal-btn");
    selectedMealId = mealIdElement ? mealIdElement.getAttribute("data-mealid") : null;

    console.log("✅ selectedMealId:", selectedMealId);

    // ✅ mealId 기준으로 댓글 조회
    if (selectedMealId) {
        loadMealComments(selectedMealId);
    }
});

// ✅ 식단 댓글 불러오기 (mealId 기준)
function loadMealComments(mealId) {
    $.ajax({
        url: `/comments/meal/${mealId}`,
        type: "GET",
        success: function (comments) {
            let commentList = document.getElementById("mealCommentList");
            commentList.innerHTML = "";

            if (!Array.isArray(comments) || comments.length === 0) {
                commentList.innerHTML = "<li>댓글이 없습니다.</li>";
                return;
            }

            comments.forEach(comment => {
                let userName = comment.isTrainer ? "트레이너" : comment.userName;
                let canDelete = comment.isOwnerOrTrainer;

                let li = document.createElement("li");
                li.innerHTML = `
    <div class="comment-left">
        <span class="comment-nickname">${userName}</span>
        <span class="comment-text">${comment.content}</span>
    </div>
    ${canDelete
                        ? `<button class="comment-delete-btn" onclick="deleteMealComment(${comment.commentId})">삭제</button>`
                        : ""}
`;

                commentList.appendChild(li);
            });
        },
        error: function () {
            alert("댓글을 불러오는 중 오류가 발생했습니다.");
        }
    });
}

// ✅ 댓글 작성
function submitMealCommentForm(event) {
    event.preventDefault();

    let contentElement = document.getElementById("mealCommentInput");
    let content = contentElement ? contentElement.value.trim() : "";

    if (!content) {
        alert("댓글을 입력하세요!");
        return;
    }

    if (!selectedMealId) {
        alert("식단이 존재하지 않습니다.");
        return;
    }

    let commentData = {
        content: content,
        mealId: selectedMealId,
        workoutId: null,
        userId: selectedUserId,
        createdAt: selectedDate + "T12:00:00"
    };

    $.ajax({
        url: "/comments/add",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify(commentData),
        success: function () {
            contentElement.value = "";
            loadMealComments(selectedMealId);
        },
        error: function () {
            alert("댓글 작성에 실패했습니다.");
        }
    });
}

// ✅ 댓글 삭제
function deleteMealComment(commentId) {
    if (!confirm("정말 삭제하시겠습니까?")) return;

    $.ajax({
        url: `/comments/${commentId}`,
        type: "DELETE",
        success: function () {
            loadMealComments(selectedMealId);
        },
        error: function () {
            alert("댓글 삭제에 실패했습니다!");
        }
    });
}
