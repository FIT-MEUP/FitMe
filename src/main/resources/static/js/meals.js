document.addEventListener("DOMContentLoaded", function () {
    let calendarEl = document.getElementById("calendar");
    let addButton = document.querySelector("#addMealButton"); // 추가 버튼 가져오기
    let selectedDate = new URLSearchParams(window.location.search).get("mealDate"); // ✅ 현재 URL에서 mealDate 가져오기

    //  ✅ URL에 mealDate가 없으면 기본적으로 오늘 날짜 설정 (페이지 첫 로딩 시)
    if (!selectedDate) {
        selectedDate = new Date().toISOString().split("T")[0]; // YYYY-MM-DD 형식
    }
    console.log("📌 현재 선택된 날짜:", selectedDate); // 🔥 디버깅용 로그 추가

    let calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: "dayGridMonth",
        selectable: true,
        aspectRatio: 1.0, // 캘린더 높이를 조정 
        height: '700px', // 한 달 캘린더가 한눈에 보이도록 자동 조정 
        headerToolbar: {			// 버튼 정렬 수정 
            left: "prev,next",
            center: "title",
            right: "today"
        },
        dateClick: function (info) {
            document.querySelectorAll(".fc-daygrid-day").forEach(day => {
                day.classList.remove("selected-date");
            });

            let clickedCell = document.querySelector(`[data-date="${info.dateStr}"]`);
            if (clickedCell) {
                clickedCell.classList.add("selected-date");
            }

            selectedDate = info.dateStr; // ✅ 선택된 날짜 저장
            document.getElementById("selectedMealDate").value = selectedDate;

            // ✅ 날짜 클릭 시 해당 날짜 페이지로 이동
            window.location.href = `/meals?mealDate=${selectedDate}`;
        }
    });

    calendar.render();

    // ✅ 현재 선택된 날짜의 식단 개수 확인하고 "추가" 버튼 비활성화 처리
    function updateAddButtonState() {
        let meals = document.querySelectorAll(".card"); // ✅ 모든 식단 카드 가져오기

        let selectedMeals = Array.from(meals).filter(meal => {
            let mealDateElement = meal.querySelector(".card-title");
            if (!mealDateElement) return false; // ✅ 예외 처리 (title이 없을 경우)

            let mealDate = mealDateElement.innerText.trim(); // ✅ 식단 날짜 가져오기
            return mealDate === selectedDate; // ✅ 현재 선택한 날짜의 식단만 카운트
        });

        if (selectedMeals.length >= 3) {
            addButton.disabled = true; // ✅ 3개 이상이면 비활성화
        } else {
            addButton.disabled = false; // ✅ 3개 미만이면 활성화
        }
    }

    updateAddButtonState(); // ✅ 페이지 로딩 시 버튼 상태 업데이트

    // ✅ 새로운 식단이 추가된 후 버튼 상태 업데이트 (모달 닫힐 때)
    document.getElementById("mealModal").addEventListener("hidden.bs.modal", function () {
        updateAddButtonState();
    });

    // ✅ 새 식단이 추가될 때 버튼 상태를 즉시 업데이트 (form submit 후에도 체크)
    document.querySelector("#mealModal form").addEventListener("submit", function () {

        console.log("🚀 폼 제출 - mealDate 값:", document.getElementById("selectedMealDate").value); // 🔥 디버깅용 로그 추가

        setTimeout(updateAddButtonState, 500); // ✅ 서버에서 데이터가 반영될 시간을 고려하여 약간의 지연 추가
    });

    // ✅ 식단이 삭제될 때 버튼 상태 업데이트 (삭제 버튼 클릭 시)
    document.querySelectorAll('form[action="/meals/delete"]').forEach(form => {
        form.addEventListener("submit", function (event) {
            event.preventDefault(); // 🔥 기본 폼 제출 방지

            let formData = new FormData(form);
            let mealDate = formData.get("mealDate"); // 🔥 삭제할 식단의 날짜 가져오기

            fetch(form.action, {
                method: form.method,
                body: formData
            }).then(() => {
                console.log("✅ 삭제 완료 - 유지할 날짜:", mealDate);
                window.location.href = `/meals?mealDate=${mealDate}`; // 🔥 삭제 후에도 같은 날짜 유지
            });
        });
    });

    // ✅ 페이지가 처음 로드될 때 한 번 실행
    document.addEventListener("DOMContentLoaded", updateAddButtonState);
});

// ✅ 모달이 열릴 때, selectedMealDate가 비어 있으면 현재 선택한 날짜를 넣어줌
let mealModal = document.getElementById("mealModal");
if (mealModal) {  // 🔥 mealModal이 존재할 경우에만 실행
    mealModal.addEventListener("show.bs.modal", function (event) {
        let selectedDateInput = document.getElementById("selectedMealDate");

        if (!selectedDate) { // ✅ 선택한 날짜가 없을 경우 모달 열림 방지
            alert("날짜를 먼저 선택하세요!");
            event.preventDefault();
            return;
        }

        if (!selectedDateInput.value) {
            selectedDateInput.value = selectedDate;
        }

        console.log("✅ 모달 열림 - selectedMealDate 값:", selectedDateInput.value);
    });
} else {
    console.log("⚠️ mealModal이 존재하지 않아 이벤트 리스너를 추가하지 않음.");
}





document.getElementById("searchFoodButton").addEventListener("click", function () {
    let query = document.getElementById("foodSearch").value;
    fetch(`/foods/search?query=${query}`)
        .then(response => response.json())
        .then(data => {
            let resultsDiv = document.getElementById("foodResults");
            resultsDiv.innerHTML = "";
            data.forEach(food => {
                let btn = document.createElement("button");
                btn.className = "btn btn-light w-100 mt-1";
                btn.textContent = `${food.foodName} (${food.calories} kcal)`;
                btn.onclick = function () {
                    document.getElementById("mealFoodName").value = food.foodName;
                    document.getElementById("mealCalories").value = food.calories;
                    document.getElementById("mealCarbs").value = food.carbs;
                    document.getElementById("mealProtein").value = food.protein;
                    document.getElementById("mealFat").value = food.fat;

                    let listItem = document.createElement("li");
                    listItem.className = "list-group-item";
                    listItem.textContent = `${food.foodName} (${food.calories} kcal)`;
                    document.getElementById("selectedFoodsList").appendChild(listItem);

                    bootstrap.Modal.getInstance(document.getElementById("foodModal")).hide();
                };
                resultsDiv.appendChild(btn);
            });
        });
});

// ✅ 수정 버튼 클릭 시, 기존 데이터가 모달에 자동으로 채워짐
document.addEventListener("click", function (event) {
    let button = event.target.closest(".edit-meal-btn"); // ✅ 버튼 내부 요소 클릭해도 인식 가능
    if (button) {
        // ✅ 수정할 데이터 가져오기
        let mealId = button.getAttribute("data-mealid");
        let mealDate = button.getAttribute("data-mealdate");
        let totalCalories = button.getAttribute("data-totalcalories");
        let totalCarbs = button.getAttribute("data-totalcarbs");
        let totalProtein = button.getAttribute("data-totalprotein");
        let totalFat = button.getAttribute("data-totalfat");

        // ✅ 수정 모달에 값 채우기
        document.getElementById("editMealId").value = mealId;
        document.getElementById("editMealDate").value = mealDate;
        document.getElementById("editTotalCalories").value = totalCalories;
        document.getElementById("editTotalCarbs").value = totalCarbs;
        document.getElementById("editTotalProtein").value = totalProtein;
        document.getElementById("editTotalFat").value = totalFat;

        // ✅ 디버깅 로그 (F12 개발자 도구에서 확인 가능)
        console.log("📌 수정 버튼 클릭됨");
        console.log("✅ mealId:", mealId);
        console.log("✅ mealDate:", mealDate);
    }
});

// ✅ 수정 모달이 열릴 때 로그 출력 (디버깅용)
let editMealModal = document.querySelector("#editMealModal");
if (editMealModal) {
    editMealModal.addEventListener("show.bs.modal", function () {
        console.log("📌 수정 모달이 열렸습니다!");
        console.log("✅ 수정할 mealId:", document.getElementById("editMealId").value);
    });
}


