// 수정 모드 상태를 관리
let isEditMode = false;

// 수정 모드 토글 함수
function toggleEditMode() {
    const userInfoRow = document.getElementById("userInfoRow");
    const editButtons = document.getElementById("editButtons");

    if (!isEditMode) {
        // 수정 모드로 전환
        isEditMode = true;
        editButtons.style.display = "block";
        
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

        // 각 셀을 입력 필드로 변환
        userInfoRow.innerHTML = `
            <td><input type="number" id="editHeight" value="${document.getElementById("height").innerText.replace(' cm', '')}"> cm</td>
            <td><input type="number" id="editWeight" value="${document.getElementById("weight").innerText.replace(' kg', '')}"> kg</td>
            <td><input type="number" step="0.1" id="editBmi" value="${document.getElementById("bmi").innerText}"></td>
            <td><input type="number" step="0.1" id="editFatMass" value="${document.getElementById("fatMass").innerText.replace('%', '')}">%</td>
            <td><input type="number" step="0.1" id="editMuscleMass" value="${document.getElementById("muscleMass").innerText.replace('kg', '')}">kg</td>
            <td><input type="number" id="editBmr" value="${document.getElementById("basalMetabolicRate").innerText.replace(' kcal', '')}"> kcal</td>
            <td>
                <select id="editYear">${yearOptions}</select>
                <select id="editMonth" onchange="updateDays()">${monthOptions}</select>
                <select id="editDay">${dayOptions}</select>
            </td>
        `;
    } else {
        cancelEdit();
    }
}

// 월 변경 시 일 수 업데이트 함수
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

// 저장 버튼 클릭 시 동작
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
    const userId = 1;
    
    // 입력받은 연,월,일을 사용해 "YYYY-MM-DD" 형식의 문자열 생성 (타임존 문제 회피)
    const formattedMonth = month.toString().padStart(2, '0');
    const formattedDay = day.toString().padStart(2, '0');
    const recordDate = `${year}-${formattedMonth}-${formattedDay}`;

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
            userId,
            recordDate
        }),
        success: function(response) {
            alert('저장 성공');
            console.log("서버 응답:", response);
        },
        error: function(xhr, status, error) {
            alert('저장 실패');
            console.error("에러 상태:", status);
            console.error("에러 내용:", error);
        }
    });
}

// 취소 버튼 클릭 시 동작
function cancelEdit() {
    location.reload();
}

//////////////////////////////////////////////////////////////여기부터 달력/////////////////////////////////////////////////////////
let currentYear = new Date().getFullYear(); // 현재 연도
let currentMonth = new Date().getMonth(); // 현재 월 (0부터 시작, 0 = 1월)
let selectedDate = new Date(); // 기본 날짜는 현재 날짜
let chartInstance = null; // 차트 인스턴스를 저장할 전역 변수



// 달력 생성 함수
function createCalendar() {
    const calendarElement = document.getElementById("calendar");
    const dateDisplay = document.getElementById("date-display");

    // 해당 월의 첫 날과 마지막 날 구하기
    const firstDay = new Date(currentYear, currentMonth, 1).getDay(); // 첫 번째 날의 요일 (0=일, 1=월, ...)
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate(); // 해당 월의 일수

    // 날짜 표시 (형식: 2025.02.19일 월요일)
    const monthName = ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'];
    const dayOfWeek = ['일', '월', '화', '수', '목', '금', '토'];
    const firstDayOfWeek = new Date(currentYear, currentMonth, 1).getDay();

    // 선택된 날짜의 형식화 
    const selectedDateText = `${currentYear}.${monthName[currentMonth]}`;
    dateDisplay.textContent = selectedDateText;

    // 달력 헤더 (요일 표시)
    const headerRow = document.createElement("tr");
    const weekdays = ['일', '월', '화', '수', '목', '금', '토'];
    weekdays.forEach(day => {
        const th = document.createElement("th");
        th.textContent = day;
        headerRow.appendChild(th);
    });
    calendarElement.innerHTML = ''; // 기존 달력 초기화
    calendarElement.appendChild(headerRow);

    // 달력 내용 (해당 월의 날짜 표시)
    let currentDay = 1;
    for (let i = 0; i < 6; i++) { // 최대 6주
        const row = document.createElement("tr");
        for (let j = 0; j < 7; j++) {
            const td = document.createElement("td");
            if (i === 0 && j < firstDay) {
                td.textContent = ''; // 첫 주의 빈 공간
            } else if (currentDay <= daysInMonth) {
                td.textContent = currentDay;

                // 현재의 날짜 값을 별도의 변수에 저장해서 캡처
                let day = currentDay;
                td.onclick = function () { onDateClick(day); }; // 날짜 클릭 이벤트


                if (currentDay === selectedDate.getDate()) {
                    td.style.backgroundColor = '#c0e0ff'; // 선택된 날짜 하이라이트
                }
                currentDay++;
            }
            row.appendChild(td);
        }
        calendarElement.appendChild(row);
    }

}
// 날짜 클릭 시 동작하는 함수
function onDateClick(day) {
    // 선택된 날짜가 해당 월에 맞게 업데이트
    selectedDate = new Date(currentYear, currentMonth, day);
    console.log(selectedDate);
    createCalendar(); // 달력 새로 그리기
    updateUserInfo(); // 사용자 정보 업데이트
}

// 사용자 정보 업데이트 함수
function updateUserInfo() {
    const dateDisplay = document.getElementById("date-display");
    const dayOfWeek = ['일', '월', '화', '수', '목', '금', '토'];
    const monthNames = ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'];
    dateDisplay.textContent = `${currentYear}.${monthNames[currentMonth]} ${selectedDate.getDate()}일 (${dayOfWeek[selectedDate.getDay()]})`;
}

// 월 변경 함수
function changeMonth(offset) {
    currentMonth += offset; // 다음/이전 월로 이동
    if (currentMonth < 0) {
        currentMonth = 11; // 1월로 돌아감
        currentYear--; // 연도 감소
    } else if (currentMonth > 11) {
        currentMonth = 0; // 12월에서 1월로 이동
        currentYear++; // 연도 증가
    }
    createCalendar(); // 달력 새로 그리기
}

// 그래프 선택 함수
function showGraph(type) {
    const graphContainer = document.getElementById("graph-container");
    const graphCanvas = document.getElementById("graphCanvas");
    graphContainer.style.display = 'block'; // 그래프 영역 보이기
    const ctx = graphCanvas.getContext('2d');
    const data = generateGraphData(type); // 그래프 데이터 생성

    // 기존에 생성된 차트가 있다면 제거
    if (chartInstance) {
        chartInstance.destroy();
    }


    // 새 차트 생성 및 전역 변수에 할당
    chartInstance = new Chart(ctx, {
        type: 'line',
        data: data,
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'top',
                },
            },
        },
    });
}


// 선택된 그래프에 맞는 데이터 생성 함수
function generateGraphData(type) {
    const labels = Array.from({ length: 30 }, (_, i) => `${i + 1} 사`);
    let data = [];

    switch (type) {
        case 'weight':
            data = [60, 62, 64, 63, 65, 66, 67, 68, 69, 70];
            break;
        case 'muscle':
            data = [10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60, 62, 64, 66, 68, 70];
            break;
        case 'fat':
            data = [20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 2, 2, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0];
            break;
    }

    return {
        labels: ['2023-03-25', '2023-03-25', '2023-03-25', '2023-03-25', '2023-03-25', '2023-03-25', '2023-03-25', '2023-03-25', '2023-03-25', '2023-03-25','2023-03-25', '2023-03-25', '2023-03-25', '2023-03-25', '2023-03-25', '2023-03-25','2023-03-25', '2023-03-25', '2023-03-25', '2023-03-25', '2023-03-25', '2023-03-25'],
        datasets: [{
            label: type === 'weight' ? '체중' : type === 'muscle' ? '골격근' : '체지방',
            data: data,
            fill: true,
            borderColor: 'rgba(75, 192, 192, 1)',
            tension: 0.1
        }]
    };
}

// 페이지 로딩 시 달력 생성
createCalendar();



