let urlParams = new URLSearchParams(window.location.search);
let selectedDate = urlParams.get("workoutDate") || new Date().toISOString().split("T")[0];

let selectedWorkoutId = null; // 수정할 workoutId 저장 (초기값: null)
let videoMap = {}; //  전역 변수로 videoMap 초기화

document.addEventListener("DOMContentLoaded", function () {
    let calendarEl = document.getElementById("calendar");

    let calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: "dayGridMonth",
        selectable: true,
        aspectRatio: 1.2,
        height: "auto",
        headerToolbar: {
            left: "prev,next",
            center: "title",
            right: "today"
        },
        dateClick: function (info) {
            selectedDate = info.dateStr;
            let newUrl = window.location.pathname + "?workoutDate=" + selectedDate;
            window.history.pushState({ path: newUrl }, "", newUrl);
            loadWorkoutData(selectedDate);
        }
    });
    calendar.render();

    loadWorkoutData(selectedDate);

    // 댓글
    let selectedDateInput = document.getElementById("selectedDate");
    selectedDateInput.value = selectedDate;

});


//  전역에서 handleSearchClick 함수 정의 (window.onload 전에!)
function handleSearchClick() {
    let query = document.getElementById("search").value.trim();
    if (!query) {
        alert("검색어를 입력하세요!");
        return;
    }
    searchWorkout(query);

    document.getElementById("workoutTable").classList.add("hidden");
    document.getElementById("searchResultsTable").classList.remove("hidden");
}

// window.onload에서 검색 버튼 이벤트 연결
window.onload = function () {

    setTimeout(() => {
        let searchBtn = document.getElementById("searchBtn");

        if (searchBtn) {
            searchBtn.removeEventListener("click", handleSearchClick); // 중복 방지
            searchBtn.addEventListener("click", handleSearchClick);
        } else {

        }
    }, 500);
};



//운동기록 불러오기 + 댓글 불러오기 추가 
function loadWorkoutData(date) {
    $.ajax({
        url: `/workout/data?workoutDate=${date}`,
        type: "GET",
        success: function (response) {
            document.getElementById("workoutTable").classList.remove("hidden");
            document.getElementById("searchResultsTable").classList.add("hidden");

            $.ajax({
                url: `/workout/videoMap?workoutDate=${date}`,
                type: "GET",
                success: function (videoResponse) {
                    updateWorkoutTable(response, videoResponse || {});

                    // ✅ 운동 기록이 있을 경우 댓글 섹션 표시
                    if (response.length > 0) {
                        document.getElementById("commentSection").style.display = "block";
                        loadComments(date);
                    } else {
                        document.getElementById("commentSection").style.display = "none";
                    }
                },
                error: function () {
                    updateWorkoutTable(response, {});

                    // ✅ 운동 기록이 없으면 댓글 섹션 숨김
                    if (response.length > 0) {
                        document.getElementById("commentSection").style.display = "block";
                        loadComments(date);
                    } else {
                        document.getElementById("commentSection").style.display = "none";
                    }
                }
            });
        },
        error: function () {
            console.error("❌ 운동 기록 불러오기 실패!");
        }
    });
}



// ✅ 운동 기록을 테이블에 업데이트하는 함수
function updateWorkoutTable(workouts, videoMap) {
    let table = document.getElementById("workoutTable").getElementsByTagName("tbody")[0];
    table.innerHTML = workouts.length === 0 ? `<tr><td colspan="7">운동 기록이 없습니다.</td></tr>` : "";

    workouts.forEach(workout => {
        let videoFile = videoMap[workout.workoutId] || null;
        let videoButton = videoFile
            ? `<button class="btn btn-sm btn-success" onclick="openVideo('${videoFile}')">🎥 영상 열기</button>`
            : `<button class="btn btn-sm btn-info" onclick="uploadVideoForWorkout(${workout.workoutId})">📂 삽입</button>`;
        let newRow = table.insertRow();
        newRow.innerHTML = `
    <td>${workout.part}</td>
    <td>${workout.exercise}</td>
    <td>${workout.sets}</td>
    <td>${workout.reps}</td>
    <td>${workout.weight}</td>
    <td>
        ${videoFile ? `<button class="btn btn-sm btn-success" onclick="openVideo('${videoFile}')">🎥 영상 열기</button>`
                : `<button class="btn btn-sm btn-info" onclick="uploadVideoForWorkout(${workout.workoutId})">📂 삽입</button>`}
    </td>
    <td>
        <button class="btn btn-sm btn-warning" onclick="editWorkout(${workout.workoutId})">수정</button>
        <button class="btn btn-sm btn-danger" onclick="deleteWorkout(${workout.workoutId})">삭제</button>
    </td>
`;

    });
}


// 수정 버튼 클릭 시, 입력 필드에 기존 데이터 채우고 버튼 상태 전환
function editWorkout(id) {
    selectedWorkoutId = id;

    $.ajax({
        url: `/workout/${id}`,
        type: "GET",
        success: function (workout) {
            console.log("✅ 기존 운동 기록 불러오기:", workout);

            if (workout) {
                $("#part").val(workout.part);
                $("#exercise").val(workout.exercise);
                $("#sets").val(workout.sets);
                $("#reps").val(workout.reps);
                $("#weight").val(workout.weight);

                $("#updateWorkoutBtn").removeClass("hidden");
                $("#addWorkoutBtn").addClass("hidden");
            } else {
                alert("🚨 운동 기록을 찾을 수 없습니다.");
            }
        },
        error: function (xhr, status, error) {
            console.error("❌ 운동 기록 불러오기 실패!", error);
            alert("운동 기록을 불러오는 중 오류 발생!");
        }
    });
}



// 수정 완료 버튼 클릭 시, PUT 요청 전송
function updateWorkout() {
    if (!selectedWorkoutId) {
        alert("수정할 운동을 선택하세요!");
        return;
    }

    let updatedWorkout = {
        part: $("#part").val(),
        exercise: $("#exercise").val(),
        sets: $("#sets").val(),
        reps: $("#reps").val(),
        weight: $("#weight").val(),
        workoutDate: selectedDate
    };
    // 🔥 AJAX 요청으로 서버에 수정된 데이터 전송
    $.ajax({
        url: `/workout/${selectedWorkoutId}`,
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify(updatedWorkout),
        success: function (response) {
            alert("✅ 운동 기록이 수정되었습니다.");
            resetForm();
            loadWorkoutData(selectedDate);  // ✅ 최신 데이터 다시 불러오기
        },
        error: function (xhr, status, error) {
            console.error("❌ 운동 기록 수정 실패!", error);
            alert("수정 중 오류 발생!");
        }
    });
}

// 입력 폼 초기화 및 버튼 상태 복구
function resetForm() {
    selectedWorkoutId = null;
    $("#part").val("");
    $("#exercise").val("");
    $("#sets").val("");
    $("#reps").val("");
    $("#weight").val("");
    $("#updateWorkoutBtn").addClass("hidden");
    $("#addWorkoutBtn").removeClass("hidden");
}

// 추가 버튼 클릭 시, POST 요청 전송 (새로운 데이터 추가)
function addWorkout() {
    let part = $("#part").val();
    let exercise = $("#exercise").val();
    let sets = $("#sets").val();
    let reps = $("#reps").val();
    let weight = $("#weight").val();
    let workoutDate = selectedDate;  // ✅ workoutDate를 전역 변수 `selectedDate`에서 가져오기

    if (!part || !exercise || !sets || !reps || !weight) {
        alert("모든 항목을 입력하세요!");
        return;
    }

    console.log("🔥 운동 등록 요청:", { part, exercise, sets, reps, weight, workoutDate }); // ✅ 디버깅용 콘솔 로그 추가

    $.ajax({
        url: "/workout",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({ part, exercise, sets, reps, weight, workoutDate }),  // ✅ workoutDate 포함
        success: function (response) {
            console.log("✅ 운동 기록 저장 완료:", response);
            resetForm();
            loadWorkoutData(workoutDate);  // ✅ 저장 후 선택된 날짜 데이터 다시 불러오기
        },
        error: function (xhr, status, error) {
            console.error("❌ 운동 기록 저장 실패:", error);
            alert("운동 기록 저장 중 오류 발생!");
        }
    });
}


// 운동 기록 삭제 기능
function deleteWorkout(workoutId) {
    if (!confirm("정말 삭제하시겠습니까?")) {
        return;
    }
    $.ajax({
        url: `/workout/delete/${workoutId}`,
        type: "POST",
        success: function (response) {
            alert("운동 기록이 삭제되었습니다.");
            loadWorkoutData(selectedDate);
        },
        error: function (xhr, status, error) {
            alert("삭제 중 오류 발생!");
        }
    });
}

// 영상 업로드 기능
function uploadVideoForWorkout(workoutId) {
    let fileInput = document.getElementById("videoFileForWorkout");
    fileInput.onchange = function () {
        let file = fileInput.files[0];
        if (!file) {
            alert("업로드할 영상을 선택하세요!");
            return;
        }

        let formData = new FormData();
        formData.append("videoFile", file);
        formData.append("workoutId", workoutId);

        $.ajax({
            url: "/workout/videos",
            type: "POST",
            data: formData,
            processData: false,
            contentType: false,
            success: function (response) {
                alert("영상이 업로드되었습니다.");
                loadWorkoutData(selectedDate);
            },
            error: function (xhr, status, error) {

                alert("영상 업로드 중 오류 발생!");
            }
        });
    };
    fileInput.click();
}

// 영상 열기 : 새 창으로 열기 
function openVideo(videoFileName) {
    let videoUrl = `/uploads/video/${videoFileName}`;
    window.open(videoUrl, "_blank", "width=800,height=600");
}


// 검색 결과를 표시할 때 기존 운동 기록 테이블을 숨기긱
// 검색 후 기존 테이블 숨기고, 검색 결과 테이블 보이기
document.getElementById("searchBtn").addEventListener("click", function () {
    let query = document.getElementById("search").value;
    searchWorkout(query);

    // 🔥 기존 운동 기록 테이블 숨기기
    document.getElementById("workoutTable").style.display = "none";
    document.getElementById("searchResultsTable").style.display = "table"; // ✅ 검색 결과 테이블 보이기
});

// 검색 결과 업데이트 시 테이블 표시 여부 제어
function updateSearchResultsTable(workouts, videoMap) {

    let title = document.getElementById("searchResultsTitle");
    let table = document.getElementById("searchResultsTable");
    let tbody = table.getElementsByTagName("tbody")[0];

    tbody.innerHTML = ""; // 기존 데이터 초기화

    if (workouts.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5">운동 기록이 없습니다.</td></tr>`;
        table.classList.remove("hidden");  // 
        table.style.display = "table";  // 
        title.classList.add("hidden");

        return;
    }

    title.classList.remove("hidden");

    workouts.forEach(workout => {
        let videoFile = videoMap[workout.workoutId] || null;
        let videoButton = videoFile
            ? `<button class="btn btn-sm btn-success" onclick="openVideo('${videoFile}')">🎥 영상 열기</button>`
            : `<button class="btn btn-sm btn-info" onclick="uploadVideoForWorkout(${workout.workoutId})">📂 삽입</button>`;

        let newRow = tbody.insertRow();
        newRow.innerHTML = `
            <td>${workout.workoutDate || "날짜 없음"}</td>
            <td>${workout.part || "부위 없음"}</td>
            <td>${workout.exercise || "운동 이름 없음"}</td>
            <td>${workout.sets || 0} / ${workout.reps || 0} / ${workout.weight || 0}</td>
            <td>${videoButton}</td>
        `;
    });



    table.classList.remove("hidden"); // ✅ hidden 클래스 제거
    table.style.display = "table"; // ✅ 강제 표시

}


// ✅ 운동 검색 기능 (AJAX 요청)
function searchWorkout(query) {
    let hasVideo = document.getElementById("videoFilter").checked;

    if (!query.trim()) {
        alert("검색어를 입력하세요!");
        return;
    }

    $.ajax({
        url: `/workout/search?query=${query}&hasVideo=${hasVideo}`,
        type: "GET",
        success: function (response) {


            if (response.length === 0) {
                updateSearchResultsTable([], {});
                return;
            }

            let workoutIds = response.map(workout => workout.workoutId);

            if (workoutIds.length === 0) {
                updateSearchResultsTable(response, {});
                return;
            }

            //  videoMap 가져오기 (검색된 운동들의 영상 정보)
            $.ajax({
                url: "/workout/videoMap",  // 🔥 GET → POST 변경
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify(workoutIds),
                success: function (videoResponse) {

                    updateSearchResultsTable(response, videoResponse);
                },
                error: function (xhr) {

                    updateSearchResultsTable(response, {});
                }
            });
        },
        error: function (xhr, status, error) {

        }
    });

}

// 
function submitCommentForm(event) {
    event.preventDefault();

    let content = document.getElementById("commentInput").value.trim();
    let displayDate = selectedDate; // ✅ 선택된 날짜 기반으로 저장

    console.log("🔥 선택된 날짜:", displayDate); // ✅ 디버깅 추가

    if (!content) {
        alert("댓글을 입력하세요!");
        return;
    }

    let commentData = {
        content: content,
        createdAt: displayDate + "T12:00:00", // ✅ 날짜 + 12시 설정 (ISO 8601 형식)
        workoutId: null,
        mealId: null
    };

    console.log("🔥 댓글 요청 데이터:", commentData); // ✅ 디버깅 추가

    $.ajax({
        url: "/comments/add",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify(commentData),
        success: function () {
            document.getElementById("commentInput").value = "";
            loadComments(displayDate);
        },
        error: function (xhr) {
            console.error("❌ 댓글 추가 실패!", xhr.responseText);
            alert("댓글 추가 실패: " + xhr.responseText);
        }
    });
}






// ✅ 날짜별 댓글 불러오기 (URL 확인)
function loadComments(date) {
    console.log("🔥 댓글 불러오기 요청 날짜: " + date); // 디버깅용 로그 추가

    $.ajax({
        url: `/comments/date/${date}`,  // ✅ API 요청할 때 날짜 올바르게 포함
        type: "GET",
        success: function (comments) {
            let commentList = document.getElementById("commentList");
            commentList.innerHTML = "";  // 기존 댓글 초기화

            if (comments.length === 0) {
                commentList.innerHTML = "<li>댓글이 없습니다.</li>";
                return;
            }

            comments.forEach(comment => {
                let li = document.createElement("li");
                li.innerHTML = `
                    <strong>${comment.userId || "익명"}</strong>: ${comment.content}
                    <button class="btn btn-sm btn-danger" onclick="deleteComment(${comment.commentId})">삭제</button>
                `;
                commentList.appendChild(li);
            });
        },
        error: function (xhr) {
            console.error("❌ 날짜별 댓글 불러오기 실패!", xhr.responseText);
        }
    });
}


// 댓글 추가
function addComment() {
    let content = document.getElementById("commentInput").value.trim();
    let selectedDate = document.getElementById("selectedDate").value || new Date().toISOString().split("T")[0]; // 🔥 기본값 설정

    if (!content) {
        alert("댓글을 입력하세요!");
        return;
    }

    $.ajax({
        url: "/comments/add",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({
            content: content,
            workoutId: selectedWorkoutId,
            displayDate: selectedDate // ✅ 날짜 포함
        }),
        success: function () {
            document.getElementById("commentInput").value = "";
            loadComments(selectedDate);
        },
        error: function (xhr) {
            console.error("❌ 댓글 추가 실패!", xhr.responseText);
            alert("댓글 추가 실패: " + xhr.responseText);
        }
    });
}



// 댓글 삭제 
function deleteComment(commentId) {
    if (!confirm("정말 삭제하시겠습니까?")) return;

    $.ajax({
        url: `/comments/${commentId}`,
        type: "DELETE",
        success: function () {
            alert("댓글이 삭제되었습니다.");
            loadComments(selectedDate);
        },
        error: function () {
            alert("댓글 삭제 실패!");
        }
    });
}

