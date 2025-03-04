document.addEventListener("DOMContentLoaded", function () {
    /** ✅ 캘린더 설정 */
    let calendarEl = document.getElementById("calendar");
    let addButton = document.querySelector("#addMealButton");
    let selectedDate = new URLSearchParams(window.location.search).get("mealDate");

    if (!selectedDate) {
        selectedDate = new Date().toISOString().split("T")[0]; // 기본값: 오늘 날짜
    }
    console.log("📌 현재 선택된 날짜:", selectedDate);

    let calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: "dayGridMonth",
        selectable: true,
        aspectRatio: 1.0,
        height: '700px',
        headerToolbar: { left: "prev,next", center: "title", right: "today" },
        dateClick: function (info) {
            document.querySelectorAll(".fc-daygrid-day").forEach(day => {
                day.classList.remove("selected-date");
            });

            let clickedCell = document.querySelector(`[data-date="${info.dateStr}"]`);
            if (clickedCell) clickedCell.classList.add("selected-date");

            selectedDate = info.dateStr;
            document.getElementById("selectedMealDate").value = selectedDate;

            window.location.href = `/meals?mealDate=${selectedDate}`;
        }
    });

    calendar.render();

    /** ✅ 식단 추가 버튼 활성화/비활성화 */
    function updateAddButtonState() {
        let meals = document.querySelectorAll(".card");
        let selectedMeals = Array.from(meals).filter(meal => {
            let mealDateElement = meal.querySelector(".card-title");
            if (!mealDateElement) return false;
            return mealDateElement.innerText.trim() === selectedDate;
        });

        addButton.disabled = selectedMeals.length >= 3;
    }

    updateAddButtonState();
    document.getElementById("mealModal").addEventListener("hidden.bs.modal", updateAddButtonState);

    document.querySelector("#mealModal form").addEventListener("submit", function () {
        console.log("🚀 폼 제출 - mealDate 값:", document.getElementById("selectedMealDate").value);
        setTimeout(updateAddButtonState, 500);
    });

    document.querySelectorAll('form[action="/meals/delete"]').forEach(form => {
        form.addEventListener("submit", function (event) {
            event.preventDefault();
            let formData = new FormData(form);
            let mealDate = formData.get("mealDate");

            fetch(form.action, { method: form.method, body: formData })
                .then(() => {
                    console.log("✅ 삭제 완료 - 유지할 날짜:", mealDate);
                    window.location.href = `/meals?mealDate=${mealDate}`;
                });
        });
    });

    /** ✅ 음식 검색 기능 */
    let foodSearchInput = document.getElementById("foodSearch");
    if (foodSearchInput) {  // ✅ foodSearch가 존재하는 경우에만 실행
        foodSearchInput.addEventListener("input", function () {
            let query = this.value.trim();
            if (query.length < 1) return;

            fetch(`/foodsearch?query=${query}`)
                .then(response => response.json())
                .then(data => {
                    let resultsDiv = document.getElementById("foodResults");
                    resultsDiv.innerHTML = "";

                    data.forEach(food => {
                        let btn = document.createElement("button");
                        btn.className = "btn btn-light w-100 mt-1";
                        btn.textContent = `${food.foodName} (${food.standardWeight}g, ${food.calories} kcal)`;
                        btn.onclick = function () {
                            selectFood(food);
                        };
                        resultsDiv.appendChild(btn);
                    });
                });
        });
    } else {
        console.error("❌ foodSearch 요소를 찾을 수 없음! HTML에 추가되어 있는지 확인하세요.");
    }

    /** ✅ 음식 선택 시 자동 입력 및 먹은 g 수 입력 칸으로 이동 */
    function selectFood(food) {
        console.log("🧐 선택된 음식 데이터:", food);

        document.getElementById("selectedFoodName").value = food.foodName;
        document.getElementById("standardWeight").value = `표준중량: ${food.standardWeight}g`;

        let userWeightInput = document.getElementById("userWeight");

        // ✅ 숫자만 추출해서 넣기 (단위 제거)
        let weightOnly = parseFloat(food.standardWeight) || 0;
        userWeightInput.value = weightOnly;

        // ✅ 기존 리스너 제거 후 추가 (중복 방지)
        userWeightInput.removeEventListener("input", handleUserWeightInput);
        userWeightInput.addEventListener("input", function () {
            handleUserWeightInput(food);
        });

        updateNutritionalValues(food, weightOnly);

        // ✅ "먹은 g 수" 입력 칸으로 자동 이동
        userWeightInput.focus();
    }


    /** ✅ 먹은 g 입력 시 자동으로 영양소 업데이트 */
    function handleUserWeightInput(food) {
        let userWeight = parseFloat(document.getElementById("userWeight").value) || 0;
        updateNutritionalValues(food, userWeight);
    }

    // 영양소 계산 
    function updateNutritionalValues(food, gram) {
        gram = parseFloat(gram) || 0;
        let ratio = gram / parseFloat(food.standardWeight) || 1;

        document.getElementById("foodCalories").value = (parseFloat(food.calories) * ratio).toFixed(1);
        document.getElementById("foodCarbs").value = (parseFloat(food.carbs) * ratio).toFixed(1);
        document.getElementById("foodProtein").value = (parseFloat(food.protein) * ratio).toFixed(1);
        document.getElementById("foodFat").value = (parseFloat(food.fat) * ratio).toFixed(1);
    }


    /** ✅ 음식 추가 버튼 클릭 시 실행 */
    let addFoodButton = document.getElementById("addFoodButton");
    if (addFoodButton) {
        addFoodButton.addEventListener("click", function () {
            let foodName = document.getElementById("selectedFoodName").value;
            let userWeight = parseFloat(document.getElementById("userWeight").value);
            let foodCalories = parseFloat(document.getElementById("foodCalories").value);
            let foodCarbs = parseFloat(document.getElementById("foodCarbs").value);
            let foodProtein = parseFloat(document.getElementById("foodProtein").value);
            let foodFat = parseFloat(document.getElementById("foodFat").value);

            if (!foodName || userWeight < 1) {
                alert("음식을 선택하고 먹은 g 수를 입력하세요!");
                return;
            }

            let foodList = document.getElementById("selectedFoodsList");
            let listItem = document.createElement("li");
            listItem.className = "list-group-item d-flex justify-content-between align-items-center";
            listItem.innerHTML = `
                <span>${foodName} (${userWeight}g, ${foodCalories} kcal)</span>
                <button class="btn btn-danger btn-sm remove-food">삭제</button>
            `;

            // ✅ 삭제 버튼 이벤트 추가
            listItem.querySelector(".remove-food").addEventListener("click", function () {
                foodList.removeChild(listItem);
                updateTotalNutrition();
            });

            foodList.appendChild(listItem);

            // ✅ 총 영양소 업데이트
            updateTotalNutrition();

            // ✅ 음식 추가 모달 닫고 새 식단 추가 모달로 이동
            let foodModal = bootstrap.Modal.getInstance(document.getElementById("foodModal"));
            foodModal.hide();

            setTimeout(() => {
                let mealModal = new bootstrap.Modal(document.getElementById("mealModal"));
                mealModal.show();
            }, 300);

            // ✅ 음식 추가 후 검색어 지우기
            document.getElementById("addFoodButton").addEventListener("click", function () {
                document.getElementById("foodSearch").value = "";
                document.getElementById("foodResults").innerHTML = "";
            });
        });


    }

    /** ✅ 총 영양소 합산 */
    function updateTotalNutrition() {
        let totalCalories = 0, totalCarbs = 0, totalProtein = 0, totalFat = 0;

        document.querySelectorAll("#selectedFoodsList li").forEach(foodItem => {
            let text = foodItem.innerText;

            let values = text.match(/(\d+(\.\d+)?)g, (\d+(\.\d+)?) kcal, (\d+(\.\d+)?)g, (\d+(\.\d+)?)g, (\d+(\.\d+)?)g/);

            if (values) {
                let calories = parseFloat(values[3]) || 0;
                let carbs = parseFloat(values[5]) || 0;
                let protein = parseFloat(values[7]) || 0;
                let fat = parseFloat(values[9]) || 0;

                totalCalories += calories;
                totalCarbs += carbs;
                totalProtein += protein;
                totalFat += fat;
            }
        });

        document.getElementById("mealCalories").value = totalCalories.toFixed(1);
        document.getElementById("mealCarbs").value = totalCarbs.toFixed(1);
        document.getElementById("mealProtein").value = totalProtein.toFixed(1);
        document.getElementById("mealFat").value = totalFat.toFixed(1);
    }


});
