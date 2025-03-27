/* ======================================
   (1) 전역 변수 / 수정 모드
====================================== */
let isEditMode = false;
let chartInstance = null; // 그래프 인스턴스 저장
// Thymeleaf에서 userId는 window.loggedInUserId로 가정

/* ======================================
   (2) 수정 모드 관련 함수
====================================== */
function toggleEditMode() {
  const userInfoRow = document.getElementById("userInfoRow");
  const editButtons = document.getElementById("editButtons");

  if (!isEditMode) {
    // 수정 모드로 전환
    isEditMode = true;
    editButtons.style.display = "block";
    $("#graph-container").hide();

    const currentYear = new Date().getFullYear();
    const currentMonth = new Date().getMonth() + 1;
    const currentDay = new Date().getDate();

    let yearOptions = "";
    for (let i = currentYear - 10; i <= currentYear + 10; i++) {
      yearOptions += `<option value="${i}" ${i === currentYear ? "selected" : ""}>${i}년</option>`;
    }

    let monthOptions = "";
    for (let i = 1; i <= 12; i++) {
      monthOptions += `<option value="${i}" ${i === currentMonth ? "selected" : ""}>${i}월</option>`;
    }

    let dayOptions = "";
    for (let i = 1; i <= 31; i++) {
      dayOptions += `<option value="${i}" ${i === currentDay ? "selected" : ""}>${i}일</option>`;
    }

    // 기존 값 추출
    const height = parseFloat(document.getElementById("height").innerText.replace(' cm', '').trim()) || 0;
    const weight = parseFloat(document.getElementById("weight").innerText.replace(' kg', '').trim()) || 0;
    const bmi = parseFloat(document.getElementById("bmi").innerText.trim()) || 0;
    const fatMass = parseFloat(document.getElementById("fatMass").innerText.replace('%', '').trim()) || 0;
    const muscleMass = parseFloat(document.getElementById("muscleMass").innerText.replace('kg', '').trim()) || 0;
    const basalMetabolicRate = parseFloat(document.getElementById("basalMetabolicRate").innerText.replace(' kcal', '').trim()) || 0;

    // 입력 필드로 전환
    userInfoRow.innerHTML = `
      <td><input type="number" id="editHeight" value="${height}"> cm</td>
      <td><input type="number" id="editWeight" value="${weight}"> kg</td>
      <td><input type="number" step="0.1" id="editBmi" value="${bmi}"></td>
      <td><input type="number" step="0.1" id="editFatMass" value="${fatMass}">%</td>
      <td><input type="number" step="0.1" id="editMuscleMass" value="${muscleMass}">kg</td>
      <td><input type="number" id="editBmr" value="${basalMetabolicRate}"> kcal</td>
      <td>
        <select id="editYear">${yearOptions}</select>
        <select id="editMonth" onchange="updateDays()">${monthOptions}</select>
        <select id="editDay">${dayOptions}</select>
      </td>
    `;
  } else {
    // 이미 수정 모드라면 취소
    cancelEdit();
  }
}

function updateDays() {
  const year = document.getElementById("editYear").value;
  const month = document.getElementById("editMonth").value;
  const daySelect = document.getElementById("editDay");

  const daysInMonth = new Date(year, month, 0).getDate();
  let dayOptions = "";
  for (let i = 1; i <= daysInMonth; i++) {
    dayOptions += `<option value="${i}">${i}일</option>`;
  }
  daySelect.innerHTML = dayOptions;
}

function saveChanges() {
  const height = document.getElementById("editHeight").value;
  const weight = document.getElementById("editWeight").value;
  const bmi = document.getElementById("editBmi").value;
  const fatMass = document.getElementById("editFatMass").value;
  const muscleMass = document.getElementById("editMuscleMass").value;
  const basalMetabolicRate = document.getElementById("editBmr").value;
  const year = document.getElementById("editYear").value;
  const month = document.getElementById("editMonth").value;
  const day = document.getElementById("editDay").value;

  const formattedMonth = month.toString().padStart(2, '0');
  const formattedDay = day.toString().padStart(2, '0');
  const recordDate = `${year}-${formattedMonth}-${formattedDay}`;

  // 화면에 즉시 반영
  const userInfoRow = document.getElementById("userInfoRow");
  userInfoRow.innerHTML = `
    <td id="height">${height} cm</td>
    <td id="weight">${weight} kg</td>
    <td id="bmi">${bmi}</td>
    <td id="fatMass">${fatMass}%</td>
    <td id="muscleMass">${muscleMass} kg</td>
    <td id="basalMetabolicRate">${basalMetabolicRate} kcal</td>
    <td id="current-date">${year}.${month}.${day}</td>
  `;

  isEditMode = false;
  document.getElementById("editButtons").style.display = "none";
  $("#graph-container").hide();

  // 서버로 저장 (예시)
  $.ajax({
    url: '/application/json',
    type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify({
      height,
      weight,
      bmi,
      fatMass,
      muscleMass,
      basalMetabolicRate,
      userId: loggedInUserId,
      recordDate
    }),
    success: function (response) {
      alert('저장 성공');
      console.log("서버 응답:", response);
    },
    error: function (xhr, status, error) {
      alert('저장 실패');
      console.error("에러 상태:", status);
      console.error("에러 내용:", error);
    }
  });
}

function cancelEdit() {
  location.reload();
}

/* ======================================
   (3) 그래프 관련 함수
====================================== */
function showGraph(type) {
  if (isEditMode) {
    console.log("❌ 수정 모드에서는 그래프를 표시하지 않음!");
    return;
  }
  $("#graph-container").show();
  console.log("📢 showGraph 실행됨! type:", type);

  $.ajax({
    url: "/user/healthDataHistory",
    type: "GET",
    data: { userId: loggedInUserId },
    success: function (data) {
      console.log("📢 AJAX 응답:", data);
      if (data.length > 0) {
        updateGraph(data, type);
      } else {
        console.log("❌ 데이터 없음! 그래프 그릴 수 없음.");
      }
    },
    error: function (xhr, status, error) {
      console.error("🔥 그래프 데이터 가져오기 실패:", error);
    }
  });
}

function updateGraph(data, type) {
  data.reverse(); // 최신 날짜가 뒤로 가도록
  const labels = data.map(entry => entry.recordDate);

  let yData = [];
  let graphLabel = "";
  let graphColor = "";
  let backgroundColor = "";

  if (type === "weight") {
    yData = data.map(entry => entry.weight);
    graphLabel = "체중";
    graphColor = "rgba(75, 192, 192, 1)";
    backgroundColor = "rgba(75, 192, 192, 0.2)";
  } else if (type === "muscle") {
    yData = data.map(entry => entry.muscleMass);
    graphLabel = "골격근";
    graphColor = "rgba(192, 75, 75, 1)";
    backgroundColor = "rgba(192, 75, 75, 0.2)";
  } else if (type === "fat") {
    yData = data.map(entry => entry.fatMass);
    graphLabel = "체지방률";
    graphColor = "rgba(75, 75, 192, 1)";
    backgroundColor = "rgba(75, 75, 192, 0.2)";
  }

  // 기존 차트 제거
  if (chartInstance) {
    chartInstance.destroy();
  }

  const ctx = document.getElementById("graphCanvas").getContext("2d");
  chartInstance = new Chart(ctx, {
    type: "line",
    data: {
      labels: labels,
      datasets: [
        {
          label: graphLabel,
          data: yData,
          borderColor: graphColor,
          backgroundColor: backgroundColor,
          borderWidth: 2,
          fill: true,
          tension: 0.1
        }
      ],
    },
    options: {
      responsive: true,
      plugins: {
        legend: { position: "top" }
      },
    },
  });
  console.log("✅ 그래프 업데이트 완료!");
}

/* ======================================
   (4) 최신 데이터 불러오기
====================================== */
function fetchLatestData() {
  $.ajax({
    url: "/user/latestHealthData",
    type: "GET",
    data: { userId: loggedInUserId },
    success: function (data) {
      if (data) {
        console.log("📢 최신 데이터 응답:", data);
        $("#height").text(data.height + " cm");
        $("#weight").text(data.weight + " kg");
        $("#bmi").text(data.bmi);
        $("#fatMass").text(data.fatMass + "%");
        $("#muscleMass").text(data.muscleMass + " kg");
        $("#basalMetabolicRate").text(data.basalMetabolicRate + " kcal");
        $("#current-date").text(data.recordDate);
      }
    },
    error: function (xhr, status, error) {
      console.error("데이터 가져오기 실패:", error);
    }
  });
}

/* ======================================
   (5) 트레이너 회원 선택
====================================== */
function handleTrainerMemberChange(userId) {
  if (!userId) return;
  window.location.href = `/mypage?userId=${userId}`;
}
window.handleTrainerMemberChange = handleTrainerMemberChange;

/* ======================================
   (6) FullCalendar 초기화
====================================== */
document.addEventListener("DOMContentLoaded", function() {
  // 최신 데이터 불러오기
  fetchLatestData();

  // FullCalendar 렌더링
  const calendarEl = document.getElementById("calendar");
  const calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: "dayGridMonth",
    headerToolbar: {
      left: "prev,next",
      center: "title",
      right: "today"
    },
    selectable: true,
  });
  calendar.render();
});

function addWorkoutDots(workoutDates) {
  document.querySelectorAll('.fc-daygrid-day').forEach(cell => {
    const date = cell.getAttribute('data-date');
    if (workoutDates.includes(date)) {
      // 이미 dot이 추가되어 있다면 중복 추가 방지
      if (!cell.querySelector('.workout-dot')) {
        let dot = document.createElement('div');
        dot.classList.add('workout-dot');
        // .fc-daygrid-day-frame 내부에 dot을 추가 (없으면 cell에 추가)
        const dayFrame = cell.querySelector('.fc-daygrid-day-frame') || cell;
        dayFrame.appendChild(dot);
      }
    }
  });
}
