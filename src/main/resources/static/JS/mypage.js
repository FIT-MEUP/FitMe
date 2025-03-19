

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
		// ✅ 기존 값에서 공백 제거 + 숫자로 변환하여 NaN 방지
		        const height = parseFloat(document.getElementById("height").innerText.replace(' cm', '').trim()) || 0;
		        const weight = parseFloat(document.getElementById("weight").innerText.replace(' kg', '').trim()) || 0;
		        const bmi = parseFloat(document.getElementById("bmi").innerText.trim()) || 0;
		        const fatMass = parseFloat(document.getElementById("fatMass").innerText.replace('%', '').trim()) || 0;
		        const muscleMass = parseFloat(document.getElementById("muscleMass").innerText.replace('kg', '').trim()) || 0; 
		        const basalMetabolicRate = parseFloat(document.getElementById("basalMetabolicRate").innerText.replace(' kcal', '').trim()) || 0;

		// 각 셀을 입력 필드로 변환
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
	const userId = loggedInUserId;
//	const userId = 1;

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
	$("#graph-container").hide();

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
			recordDate,
			
		}),
		success: function(response) {
			alert('저장 성공');
			console.log("서버 응답:", response);

			isEditMode = false;

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
				td.onclick = function() { onDateClick(day); }; // 날짜 클릭 이벤트


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

////////////////////////////////////////////////////////////달력끝/////////////////////////////////////////////////////////


	function showGraph(type) {
		if (isEditMode) {
			console.log("❌ 수정 모드에서는 그래프를 표시하지 않음!");
			return;
		}
		$("#graph-container").show();
		console.log("📢 showGraph 실행됨! type:", type);  // ✅ 함수 실행 확인
	
		$.ajax({
			url: "/user/healthDataHistory", // 📌 DB에서 건강 데이터 가져오기
			type: "GET",
			data: { userId: loggedInUserId},  // 현재는 userId=1로 가정
			success: function(data) {
				console.log("📢 AJAX 응답:", data); // ✅ 데이터 정상 수신 확인
	
				if (data.length > 0) {
					updateGraph(data, type); // ✅ 버튼에 따라 적절한 데이터만 출력
				} else {
					console.log("❌ 데이터 없음! 그래프 그릴 수 없음.");
				}
			},
			error: function(xhr, status, error) {
				console.error("🔥 그래프 데이터 가져오기 실패:", error);
			}
		});
	}





function updateGraph(data, type) {
	data.reverse();
	const labels = data.map(entry => entry.recordDate); // X축 (날짜)
	let yData = [];

	let graphLabel = "";
	let graphColor = "";

	if (type === "weight") {
		yData = data.map(entry => entry.weight);
		graphLabel = "체중";
		graphColor = "rgba(75, 192, 192, 1)"; // 청록색
		backgroundColor = "rgba(75, 192, 192, 0.2)";
	} else if (type === "muscle") {
		yData = data.map(entry => entry.muscleMass);
		graphLabel = "골격근";
		graphColor = "rgba(192, 75, 75, 1)"; // 붉은색
		backgroundColor = "rgba(192, 75, 75, 0.2)";
	} else if (type === "fat") {
		yData = data.map(entry => entry.fatMass);
		graphLabel = "체지방률";
		graphColor = "rgba(75, 75, 192, 1)"; // 파란색
		backgroundColor = "rgba(75, 75, 192, 0.2)";
	}

	console.log("📢 X축 라벨:", labels);
	console.log("📢 Y축 데이터:", yData);

	const canvas = document.getElementById("graphCanvas");
	if (!canvas) {
		console.error("❌ 오류: graphCanvas 요소를 찾을 수 없습니다!");
		return;
	}

	const ctx = document.getElementById("graphCanvas").getContext("2d");

	if (chartInstance) {
		console.log("🗑 기존 차트 삭제!");
		chartInstance.destroy(); // 기존 차트 삭제

	}
	console.log("🛠 새 차트 생성 중...");

	chartInstance = new Chart(ctx, {
		type: "line",
		data: {
			labels: labels, // X축 (날짜들)
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
				legend: {
					position: "top",
				},
			},
		},
	});

	console.log("✅ 그래프 업데이트 완료!");
}



// 페이지 로딩 시 달력 생성
createCalendar();

// ✅ 🔥 최신 데이터 자동 로드 기능 추가!
document.addEventListener("DOMContentLoaded", function() {
	fetchLatestData();  // 🔥 최신 데이터 불러오기
});


// ✅ 최신 데이터 불러오는 함수
function fetchLatestData() {
	$.ajax({
		url: "/user/latestHealthData",
		type: "GET",
		data: { userId: loggedInUserId },  // ✅ userId 추가
		success: function(data) {
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
		error: function(xhr, status, error) {
			console.error("데이터 가져오기 실패:", error);
		}
	});
}

// ✅ 그래프 데이터 불러오는 함수
function fetchGraphData() {
	$.ajax({
		url: "/user/healthDataHistory",
		type: "GET",
		data: { userId: loggedInUserId },  // 현재는 userId=1로 가정
		success: function(data) {
			if (data.length > 0) {
				updateGraph(data);
			} else {
				console.log("데이터 없음");
			}
		},
		error: function(xhr, status, error) {
			console.error("그래프 데이터 가져오기 실패:", error);
		}
	});
}

document.addEventListener("DOMContentLoaded", function() {
	fetchLatestData();  // 🔥 최신 데이터 불러오기 

});





