let loggedInUserId = document.getElementById("loggedInUserId").value;

let isTrainer = document.getElementById("isTrainer").value === "true"; // Trainer 여부 (Boolean)
let approvedUserIds = [];

let urlParams = new URLSearchParams(window.location.search);
let selectedDate = urlParams.get("workoutDate") || new Date().toISOString().split("T")[0];

let selectedWorkoutId = null; // 수정할 workoutId 저장 (초기값: null)
let videoMap = {}; //  전역 변수로 videoMap 초기화

let selectedUserId = urlParams.get("userId") || loggedInUserId;

let calendar;

let editModeWorkoutId = null;

document.addEventListener("DOMContentLoaded", function () {

    highlightSelectedDate(selectedDate);

    // 검색 버튼 이벤트 등록 추가!
    const searchBtn = document.getElementById("searchBtn");
    if (searchBtn) {
        searchBtn.addEventListener("click", function () {
            const query = document.getElementById("search").value;
            searchWorkout(query);
            document.getElementById("workoutTable").classList.add("hidden");
            document.getElementById("searchResultsTable").classList.remove("hidden");
        });
    }


    let today = new Date().toISOString().split("T")[0]; // 오늘 날짜 (YYYY-MM-DD)
    let mainPageLink = document.getElementById("mainPageLink");

    if (mainPageLink) {
        mainPageLink.href = `/work?workoutDate=${today}`; // 운동 게시판 클릭 시, 오늘 날짜로 이동
    }

    let calendarEl = document.getElementById("calendar");

    calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: "dayGridMonth",
        selectable: true,
        aspectRatio: 1.0,
        height: "700px",
        headerToolbar: {
            left: "prev,next",
            center: "title",
            right: "today"
        },
        dateClick: function (info) {
            selectedDate = info.dateStr;
            let newUrl = window.location.pathname + "?workoutDate=" + selectedDate;

            // ✅ 특정 회원 선택 시 userId 유지
            if (selectedUserId) {
                newUrl += `&userId=${selectedUserId}`;
            }

            window.history.pushState({ path: newUrl }, "", newUrl);
            loadWorkoutData(selectedDate);
            highlightSelectedDate(info.dateStr);
        }
    });

    // ✅ 캘린더 날짜 변경 시 dot 업데이트
    calendar.on('datesSet', function (info) {
        const year = info.start.getFullYear();
        const month = info.start.getMonth() + 1; // JS는 0부터 시작하므로 +1
        updateApprovedUserIds();
        fetchWorkoutDates(year, month);
    });

    calendar.render();
    updateApprovedUserIds();
    const currentDate = calendar.getDate();
    fetchWorkoutDates(currentDate.getFullYear(), currentDate.getMonth() + 1);


    loadWorkoutData(selectedDate);



    // 댓글
    let selectedDateInput = document.getElementById("selectedDate");
    selectedDateInput.value = selectedDate;

});

// 캘린더 날짜 클릭 시 선택된 날짜에만 highlight 적용
function highlightSelectedDate(dateStr) {
    document.querySelectorAll(".fc-daygrid-day").forEach(day => {
        day.classList.remove("selected-date"); // 모든 날짜에서 제거
        if (day.getAttribute("data-date") === dateStr) {
            day.classList.add("selected-date"); // 선택한 날짜만 추가
        }
    });
}
// ✅ 운동 기록 있는 날짜 조회 및 캘린더에 dot 추가
function fetchWorkoutDates(year, month) {
    let userId = selectedUserId || loggedInUserId;
    console.log("✅ fetchWorkoutDates 호출됨! year:", year, "month:", month, "userId:", userId);

    $.ajax({
        url: `/workout/highlight-dates?userId=${userId}&year=${year}&month=${month}`,
        type: "GET",
        success: function (dateList) {
            console.log("✅ API 응답 받은 날짜 목록:", dateList);

            const dots = document.querySelectorAll(".fc-daygrid-day .workout-dot");
            dots.forEach(dot => dot.remove());

            document.querySelectorAll(".fc-daygrid-day").forEach(cell => {
                const date = cell.getAttribute("data-date");

                if (dateList.includes(date)) {
                    console.log("✨ dot 추가되는 날짜:", date);
                    const dot = document.createElement("div");
                    dot.className = "workout-dot";
                    const target = cell.querySelector(".fc-daygrid-day-frame");
                    if (target) {
                        target.appendChild(dot);
                    }
                }
            });
        },
        error: function (xhr) {
            console.error("❌ 운동 기록 날짜 불러오기 실패! ", xhr);
        }
    });
}




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


// 트레이너가 회원 선택 시, userId를 포함하여 페이지 갱신
// 트레이너가 특정 회원을 선택할 때, URL을 변경하여 유지
function handleTrainerMemberChange(userId) {
    console.log("🔍 선택된 회원 ID: ", userId);
    selectedUserId = userId; // 🔥 전역 변수에 저장

    let currentUrl = new URL(window.location.href);
    currentUrl.searchParams.set('userId', userId);
    window.history.pushState({}, "", currentUrl.toString()); // ✅ URL은 변경하지만 페이지 이동 없음

    loadWorkoutData(selectedDate); // ✅ 특정 회원 선택 후 운동 기록 다시 로드
}



//  approvedUserIds를 최신 상태로 업데이트하는 함수 추가
function updateApprovedUserIds() {
    if (isTrainer) {
        let trainerMemberSelect = document.getElementById("trainerMemberSelect");
        if (trainerMemberSelect) {
            approvedUserIds = Array.from(trainerMemberSelect.options)
                .filter(option => option.value)
                .map(option => Number(option.value)); // 모든 회원 ID를 저장
        }
    }
}

//  운동 기록 불러올 때 approvedUserIds도 함께 업데이트
function loadWorkoutData(date) {
    let urlParams = new URLSearchParams(window.location.search);
    let userId = urlParams.get("userId") || selectedUserId || loggedInUserId;

    if (isTrainer) {
        userId = selectedUserId || loggedInUserId; // 트레이너는 회원 선택 가능
        updateApprovedUserIds(); //  최신 승인 회원 목록 업데이트
    } else {
        userId = loggedInUserId; // 일반 사용자는 본인 ID만 사용
    }

    console.log("🔍 조회할 userId:", userId);
    console.log("🔍 approvedUserIds 업데이트:", approvedUserIds); // ✅ approvedUserIds 최신 값 확인

    document.getElementById("workoutTable").classList.remove("hidden");
    document.getElementById("searchResultsTable").classList.add("hidden");

    $.ajax({
        url: `/workout/data?workoutDate=${date}&userId=${userId}`,
        type: "GET",
        success: function (response) {
            $.ajax({
                url: `/workout/videoMap?workoutDate=${date}&userId=${userId}`,
                type: "GET",
                success: function (videoResponse) {
                    console.log("🎥 불러온 영상 데이터:", videoResponse);
                    updateWorkoutTable(response, videoResponse || {});
                },
                error: function () {
                    updateWorkoutTable(response, {});
                }
            });
        },
        error: function () {
            console.error("❌ 운동 기록 불러오기 실패!");
        }
    });
}


// ✅ updateWorkoutTable 수정 (approvedUserIds 최신 값 사용)
function updateWorkoutTable(workouts, videoMap) {
    let table = document.getElementById("workoutTable").getElementsByTagName("tbody")[0];
    table.innerHTML = workouts.length === 0 ? `<tr><td colspan="7">운동 기록이 없습니다.</td></tr>` : "";

    console.log("✅ 불러온 운동 기록:", workouts);
    console.log("✅ updateWorkoutTable 호출됨. 운동 기록 개수:", workouts.length);

    const workoutTable = document.getElementById("workoutTable").getElementsByTagName("tbody")[0];
    workoutTable.innerHTML = "";  // 기존 내용 제거

    console.log("📌 받은 workout 데이터:", workouts);
    console.log("📌 받은 videoMap 데이터:", videoMap);
    console.log("🔍 최신 approvedUserIds:", approvedUserIds); // ✅ 최신 approvedUserIds 확인

    let userDropdown = document.getElementById("trainerMemberSelect");
    let selectedUserId = userDropdown && userDropdown.value ? Number(userDropdown.value) : Number(document.getElementById("loggedInUserId").value);
    console.log("🔍 selectedUserId:", selectedUserId);

    let firstWorkoutId = null; // 🟢 여기로 이동

    workouts.forEach((workout, index) => {
        let workoutUserId = Number(workout.userId);

        // ✅ 요청한 userId와 데이터 userId가 일치하는지 확인
        if (workoutUserId !== selectedUserId) {
            console.warn(`⚠️ userId 불일치: 요청한 userId=${selectedUserId}, 응답 데이터 userId=${workoutUserId}`);
            return;
        }

        let videoFile = videoMap[workout.workoutId] || null;
        let isOwner = workoutUserId === Number(document.getElementById("loggedInUserId").value);
        let isTrainerOfThisUser = isTrainer && approvedUserIds.includes(workoutUserId);

        console.log(`✅ isTrainerOfThisUser 체크: workout.userId=${workoutUserId}, 포함 여부=${isTrainerOfThisUser}`);

        let newRow = workoutTable.insertRow();
        newRow.setAttribute("data-workout-id", workout.workoutId);

        newRow.innerHTML = `
            <td>${workout.part}</td>
            <td>${workout.exercise}</td>
            <td>${workout.sets}</td>
            <td>${workout.reps}</td>
            <td>${workout.weight}</td>
            <td id="videoSection-${workout.workoutId}">
                ${videoFile
                ? `<button class="btn btn-sm btn-success" onclick="openVideo('${videoFile}')">🎥 영상 열기</button>`
                : `<button class="btn btn-sm btn-info" onclick="uploadVideoForWorkout(${workout.workoutId})">📂 삽입</button>`
            }
            </td>
            <td>
                ${isOwner || isTrainerOfThisUser ? `
                    <button class="btn btn-sm btn-warning" onclick="editWorkout(${workout.workoutId})">수정</button>
                    <button class="btn btn-sm btn-danger" onclick="deleteWorkout(${workout.workoutId})">삭제</button>
                ` : ''}
            </td>
        `;

        // 첫 번째 workoutId만 저장
        if (firstWorkoutId === null) {
            firstWorkoutId = workout.workoutId;
        }

        // ✅ 수정, 삭제 버튼 클릭 시 요청되는 userId 로그 출력
        newRow.querySelectorAll("button").forEach(button => {
            button.addEventListener("click", function () {
                console.log(`🔍 버튼 클릭됨: ${this.innerText}, 요청 userId=${selectedUserId}, workoutUserId=${workoutUserId}`);
            });
        });
    });

    // 운동 기록이 있을 경우, 첫 번째 운동에 대한 댓글 로딩
    if (firstWorkoutId) {
        selectedWorkoutId = firstWorkoutId;
        loadWorkoutComments(firstWorkoutId);
    } else {
        document.getElementById("commentSection").style.display = "none";
    }
}


// 수정 버튼 클릭 시, 입력 필드에 기존 데이터 채우고 버튼 상태 전환
function editWorkout(id) {
    selectedWorkoutId = id;
    editModeWorkoutId = id; // 수정 모드 활성화

    $.ajax({
        url: `/workout/${id}`,
        type: "GET",
        success: function (workout) {
            if (workout) {
                // 폼에 값 세팅 (기존 유지)
                $("#part").val(workout.part);
                $("#exercise").val(workout.exercise);
                $("#sets").val(workout.sets);
                $("#reps").val(workout.reps);
                $("#weight").val(workout.weight);

                // 버튼 상태 전환
                $("#updateWorkoutBtn").removeClass("hidden");
                $("#addWorkoutBtn").addClass("hidden");

                // 🟢 모든 수정/삭제 버튼 숨김
                $("#workoutTable tbody tr td:last-child button").addClass("hidden");

                // 기존 동작 유지
                loadWorkoutVideo(id);
            } else {
                alert("🚨 운동 기록을 찾을 수 없습니다.");
            }
        },
        error: function () {
            alert("운동 기록 불러오기 실패!");
        }
    });
}


function loadWorkoutVideo(workoutId) {
    $.ajax({
        url: `/workout/video/${workoutId}`,
        type: "GET",
        success: function (videoFileName) {
            let videoSection = document.getElementById(`videoSection-${workoutId}`);
            videoSection.innerHTML = "";

            if (videoFileName && videoFileName !== "null") {
                // 수정 모드일 때 → 삭제 버튼만
                if (editModeWorkoutId === workoutId) {
                    videoSection.innerHTML = `
                        <button class="btn btn-sm btn-danger" onclick="deleteWorkoutVideo(${workoutId})">❌ 삭제</button>
                    `;
                } else {
                    // 수정 모드 아닐 때 → 영상 열기 버튼만
                    videoSection.innerHTML = `
                        <button class="btn btn-sm btn-success" onclick="openVideo('${videoFileName}')">🎥 영상 열기</button>
                    `;
                }
            } else {
                // 영상 없을 때는 삽입 버튼
                videoSection.innerHTML = `
                    <button class="btn btn-sm btn-info" onclick="uploadVideoForWorkout(${workoutId})">📂 삽입</button>
                `;
            }
        },
        error: function () {
            console.log("❌ 동영상 정보 없음");
        }
    });
}



// ✅ 운동 기록에 연결된 동영상 삭제
function deleteWorkoutVideo(workoutId) {
    if (!confirm("정말 삭제하시겠습니까?")) return;

    let urlParams = new URLSearchParams(window.location.search);
    let userId = urlParams.get("userId") || loggedInUserId; // 트레이너가 특정 회원을 선택한 경우 포함

    $.ajax({
        url: `/workout/video/${workoutId}`,
        type: "DELETE",
        contentType: "application/json",
        data: JSON.stringify({ userId: userId }), // ✅ userId 추가
        success: function (response) {
            alert(response);
            let videoSection = document.getElementById(`videoSection-${workoutId}`);

            // ✅ 영상 삭제 후, 삽입 버튼으로 UI 즉시 변경
            videoSection.innerHTML = `
                <button class="btn btn-sm btn-info" onclick="uploadVideoForWorkout(${workoutId})">📂 삽입</button>
            `;
        },
        error: function (xhr) {
            alert("❌ 삭제 실패: " + xhr.responseText);
        }
    });
}



// ✅ 새로운 동영상 업로드
function uploadNewVideo(workoutId) {
    let fileInput = document.getElementById("videoFileForWorkout");
    let file = fileInput.files[0];

    if (!file) {
        alert("업로드할 영상을 선택하세요!");
        return;
    }

    let urlParams = new URLSearchParams(window.location.search);
    let userId = urlParams.get("userId") || loggedInUserId; // 트레이너가 특정 회원을 선택한 경우 포함

    let formData = new FormData();
    formData.append("videoFile", file);
    formData.append("workoutId", workoutId);
    formData.append("userId", userId);

    $.ajax({
        url: "/workout/video",
        type: "POST",
        data: formData,
        processData: false,
        contentType: false,
        success: function () {
            alert("영상이 업로드되었습니다.");
            loadWorkoutVideo(workoutId);
        },
        error: function () {
            alert("영상 업로드 중 오류 발생!");
        }
    });
}


// 수정 완료 버튼 클릭 시, PUT 요청 전송
function updateWorkout() {
    if (!selectedWorkoutId) {
        console.warn("⚠️ selectedWorkoutId가 설정되지 않음 → 댓글 불러오기 중단");
        return;
    }
    console.log("🔥 loadWorkoutComments 호출 - selectedWorkoutId:", selectedWorkoutId);

    let urlParams = new URLSearchParams(window.location.search);
    let userId = urlParams.get("userId") || loggedInUserId; // 트레이너가 특정 회원을 선택한 경우 포함

    let updatedWorkout = {
        part: $("#part").val(),
        exercise: $("#exercise").val(),
        sets: $("#sets").val(),
        reps: $("#reps").val(),
        weight: $("#weight").val(),
        workoutDate: selectedDate,
        userId: userId
    };

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
    editModeWorkoutId = null; // 수정 모드 비활성화
    $("#part").val("");
    $("#exercise").val("");
    $("#sets").val("");
    $("#reps").val("");
    $("#weight").val("");

    // 버튼 상태 복원
    $("#updateWorkoutBtn").addClass("hidden");
    $("#addWorkoutBtn").removeClass("hidden");

    // 🟢 모든 수정/삭제 버튼 복구
    $("#workoutTable tbody tr td:last-child button").removeClass("hidden");
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

    let urlParams = new URLSearchParams(window.location.search);
    let userId = urlParams.get("userId") || loggedInUserId; // 트레이너가 특정 회원을 선택한 경우 포함

    console.log("🔥 운동 등록 요청:", { part, exercise, sets, reps, weight, workoutDate, userId: loggedInUserId }); // ✅ 디버깅용 콘솔 로그 추가

    $.ajax({
        url: "/workout",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({ part, exercise, sets, reps, weight, workoutDate, userId }),  // ✅ workoutDate 포함
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
    let urlParams = new URLSearchParams(window.location.search);
    let userId = urlParams.get("userId") || loggedInUserId; // 트레이너가 특정 회원을 선택한 경우 포함

    $.ajax({
        url: `/workout/delete/${workoutId}`,
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({ userId: userId }), // ✅ userId 추가
        success: function (response) {
            alert("✅ 운동 기록이 삭제되었습니다.");
            loadWorkoutData(selectedDate);
        },
        error: function (xhr, status, error) {
            alert("❌ 삭제 중 오류 발생!");
        }
    });
}

// 영상 업로드 기능
function uploadVideoForWorkout(workoutId) {
    let fileInput = document.getElementById("videoFileForWorkout");

    // ✅ 기존 input이 없다면 새로 생성
    if (!fileInput) {
        fileInput = document.createElement("input");
        fileInput.type = "file";
        fileInput.id = "videoFileForWorkout"; // 새로 추가
        fileInput.style.display = "none"; // UI에서 숨기기
        document.body.appendChild(fileInput); // body에 추가
    }

    fileInput.onchange = function () {
        let file = fileInput.files[0];
        if (!file) {
            alert("업로드할 영상을 선택하세요!");
            return;
        }

        let formData = new FormData();
        formData.append("videoFile", file);
        formData.append("workoutId", workoutId);
        formData.append("userId", loggedInUserId); // ✅ 변경된 부분

        $.ajax({
            url: "/workout/upload/video",
            type: "POST",
            data: formData,
            processData: false,
            contentType: false,
            success: function () {
                alert("✅ 영상이 업로드되었습니다.");

                loadWorkoutVideo(workoutId);

                // ✅ 수정 버튼을 누르지 않은 경우
                if (selectedWorkoutId !== workoutId) {
                    videoSection.innerHTML = `
                        <button class="btn btn-sm btn-success" onclick="openVideo('${file.name}')">🎥 영상 열기</button>
                        <button class="btn btn-sm btn-danger" onclick="deleteWorkoutVideo(${workoutId})">❌ 삭제</button>
                    `;
                } else {
                    // ✅ 수정 버튼을 누른 경우 (삭제 버튼만 유지)
                    videoSection.innerHTML = `
                        <button class="btn btn-sm btn-danger" onclick="deleteWorkoutVideo(${workoutId})">❌ 삭제</button>
                    `;
                }
            },
            error: function () {
                alert("❌ 영상 업로드 중 오류 발생!");
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
        table.classList.remove("hidden");
        table.style.display = "table";
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
            <td>
                <a href="/work?workoutDate=${workout.workoutDate}&userId=${selectedUserId}">
                    ${workout.workoutDate || "날짜 없음"}
                </a>
            </td>
            <td>${workout.part || "부위 없음"}</td>
            <td>${workout.exercise || "운동 이름 없음"}</td>
            <td>${workout.sets || 0} / ${workout.reps || 0} / ${workout.weight || 0}</td>
            <td>${videoButton}</td>
        `;
    });

    table.classList.remove("hidden"); // ✅ hidden 클래스 제거
    table.style.display = "table"; // ✅ 강제 표시
}

// 운동 검색 기능 (AJAX 요청)
function searchWorkout(query) {

    console.log("✅ 검색 호출됨, query:", query);
    let hasVideo = document.getElementById("videoFilter").checked;
    let urlParams = new URLSearchParams(window.location.search);
    let userId = urlParams.get("userId") || selectedUserId || loggedInUserId;

    if (!query.trim()) {
        alert("검색어를 입력하세요!");
        return;
    }

    // 🟢 트레이너가 회원을 선택하지 않은 경우 차단
    if (isTrainer && (!urlParams.get("userId") || userId === loggedInUserId)) {
        alert("트레이너는 먼저 회원을 선택해야 검색할 수 있습니다.");
        return;
    }

    $.ajax({
        url: `/workout/search?query=${query}&hasVideo=${hasVideo}&userId=${userId}`,
        type: "GET",
        success: function (response) {
            console.log("✅ 서버에서 받은 검색결과:", response);
            if (response.length === 0) {
                updateSearchResultsTable([], {});
                return;
            }

            let workoutIds = response.map(workout => workout.workoutId);

            if (workoutIds.length === 0) {
                updateSearchResultsTable(response, {});
                return;
            }

            // videoMap 가져오기
            $.ajax({
                url: "/workout/videoMap",
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify(workoutIds),
                success: function (videoResponse) {
                    updateSearchResultsTable(response, videoResponse);
                },
                error: function () {
                    updateSearchResultsTable(response, {});
                }
            });
        },
        error: function () {
            alert("❌ 검색 중 오류 발생!");
        }
    });
}



// 댓글 ~~~~~~~~~~~~~~~~~

// ✅ 첫 번째 workoutId 가져오기 (운동 기록이 있는 경우만)
function getFirstWorkoutId() {
    setTimeout(() => {
        let firstWorkoutRow = document.querySelector("#workoutTable tbody tr[data-workout-id]");

        if (!firstWorkoutRow) {
            console.warn("⚠️ 운동 기록 없음 → workoutId 반환 실패");
            return null;
        }

        let workoutId = firstWorkoutRow.getAttribute("data-workout-id");
        console.log("🔥 첫 번째 workoutId 가져오기:", workoutId);

        if (workoutId) {
            loadWorkoutComments(workoutId); // ✅ workoutId를 가져온 후 댓글 로딩
        }

        return workoutId;
    }, 500); // 🕒 운동 기록 로드 후 0.5초 기다림
}

//
function submitCommentForm(event) {
    event.preventDefault();

    let content = document.getElementById("commentInput").value.trim();
    let workoutId = selectedWorkoutId || getFirstWorkoutId();

    if (!content) {
        alert("댓글을 입력하세요!");
        return;
    }

    let commentData = {
        content: content,
        workoutId: workoutId
    };

    $.ajax({
        url: "/comments/add",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify(commentData),
        success: function (response) {
            document.getElementById("commentInput").value = "";

            // ✅ UI에 즉시 추가
            appendNewComment(response);
        },
        error: function (xhr) {
            console.error("❌ 운동 댓글 추가 실패!", xhr.responseText);
            alert("댓글 추가 실패: " + xhr.responseText);
        }
    });
}


function appendNewComment(comment) {
    let commentList = document.getElementById("commentList");

    // 🔥 "댓글이 없습니다." 메시지가 있을 경우 삭제
    let emptyMsg = document.querySelector('#commentList li');
    if (emptyMsg && emptyMsg.textContent.includes("댓글이 없습니다.")) {
        emptyMsg.remove();
    }

    let li = document.createElement("li");
    li.id = `comment-${comment.commentId}`;

    let userDisplayName = comment.isTrainer ? "트레이너" : comment.userName;

    li.innerHTML = `
    <div class="comment-left">
        <span class="comment-nickname">${userDisplayName}</span>
        <span class="comment-text">${comment.content}</span>
    </div>
    ${comment.isOwnerOrTrainer ? `<button class="comment-delete-btn" onclick="deleteWorkoutComment(${comment.commentId})">삭제</button>` : ""}
`;


    commentList.appendChild(li);

    document.getElementById("commentSection").style.display = "block";
}




// ✅ 날짜별 댓글 불러오기 (URL 확인)
function loadWorkoutComments(workoutId) {
    if (!workoutId) {
        console.warn("⚠️ workoutId가 없음 → 댓글 불러오기 중단");
        return;
    }

    console.log(`🔥 댓글 불러오기 - workoutId: ${workoutId}`);

    $.ajax({
        url: `/comments/workout/${workoutId}`,  // ✅ 날짜 제거!
        type: "GET",
        success: function (comments) {
            let commentList = document.getElementById("commentList");
            commentList.innerHTML = "";

            if (comments.length === 0) {
                commentList.innerHTML = "<li>댓글이 없습니다.</li>";
            } else {
                comments.forEach(comment => appendNewComment(comment));
            }

            document.getElementById("commentSection").style.display = "block";
        },
        error: function (xhr) {
            console.error("❌ 운동 댓글 불러오기 실패!", xhr.responseText);
        }
    });
}



// ✅ 동적 댓글 삭제 함수 (기존 코드 수정)
function deleteWorkoutComment(commentId) {
    if (!confirm("정말 삭제하시겠습니까?")) return;

    $.ajax({
        url: `/comments/${commentId}`,
        type: "DELETE",
        success: function () {
            alert("✅ 댓글이 삭제되었습니다.");

            // ✅ 삭제된 댓글을 UI에서 즉시 제거
            let commentElement = document.getElementById(`comment-${commentId}`);
            if (commentElement) {
                commentElement.remove(); // 🔥 해당 댓글 DOM에서 제거
            }

        },
        error: function () {
            alert("❌ 댓글 삭제 실패!");
        }
    });
}
