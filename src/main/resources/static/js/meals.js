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

    /**  캘린더에서 이미 선택된 날짜 강조 */
    setTimeout(() => {
        let selectedCell = document.querySelector(`[data-date="${selectedDate}"]`);
        if (selectedCell) {
            selectedCell.classList.add("selected-date");
        }
    }, 300);


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
                    window.location.href = `/meals?mealDate=${mealDate}`;
                });
        });
    });







    console.log("✅ DOM이 로드되었습니다!");

    let mealTypeSelect = document.getElementById("mealType");
    if (mealTypeSelect) {
        mealTypeSelect.addEventListener("change", function () {
            console.log("🚀 선택된 식사 유형:", this.value);
        });
    } else {
        console.error("❌ mealType 요소를 찾을 수 없음!");
    }

    /** ✅ 음식 검색 기능 */
    setTimeout(() => {
        let foodSearchInput = document.getElementById("foodSearch");
        if (foodSearchInput) {
            console.log("✅ 음식 검색창을 찾았습니다.");
            foodSearchInput.addEventListener("input", function () {
                let query = this.value.trim();
                if (query.length < 1) {
                    document.getElementById("foodResults").innerHTML = "";
                    return;
                }

                fetch(`/foodsearch?query=${query}`)
                    .then(response => response.json())
                    .then(data => {
                        let resultsDiv = document.getElementById("foodResults");
                        resultsDiv.innerHTML = "";

                        if (data.length === 0) {
                            resultsDiv.innerHTML = `<p class="text-muted">검색 결과 없음</p>`;
                            return;
                        }

                        data.forEach(food => {
                            let btn = document.createElement("button");
                            btn.className = "btn btn-light w-100 mt-1";
                            btn.textContent = `${food.foodName}`;
                            btn.onclick = function () {
                                selectFood(food);
                            };
                            resultsDiv.appendChild(btn);
                        });
                    })
                    .catch(error => {
                        console.error("❌ 음식 검색 중 오류 발생:", error);
                    });
            });
        } else {
            console.error("❌ foodSearch 요소를 찾을 수 없음!");
        }
    }, 500);  // 0.5초 지연 실행



    /** ✅ 직접 입력 버튼 */
    document.getElementById("enableManualEntry").addEventListener("click", function () {
        isManualEntry = true;
        resetFoodInputFields();
        setReadOnly(false); // 🔹 직접 입력 모드에서는 입력 가능하게 변경

        let userWeightInput = document.getElementById("userWeight");
        if (userWeightInput) {
            userWeightInput.removeEventListener("input", handleUserWeightInput); // 🔹 자동 계산 기능 비활성화
        }
    });

    // `readonly` 속성을 설정/해제하는 함수
    function setReadOnly(state) {
        let inputs = ["foodCalories", "foodCarbs", "foodProtein", "foodFat"];
        let foodNameInput = document.getElementById("selectedFoodName");

        inputs.forEach(id => {
            let input = document.getElementById(id);
            if (input) {
                if (state) {
                    input.setAttribute("readonly", true); // 🔹 자동 입력 모드에서는 읽기 전용
                } else {
                    input.removeAttribute("readonly"); // 🔹 직접 입력 모드에서는 입력 가능
                }
            }
        });

        // 🔹 직접 입력 모드에서는 음식 이름 필드는 입력 가능하도록 설정
        if (foodNameInput) {
            if (state) {
                foodNameInput.setAttribute("readonly", true); // 자동 입력 모드에서는 읽기 전용
            } else {
                foodNameInput.removeAttribute("readonly"); // 직접 입력 모드에서는 입력 가능
            }
        }
    }

    /** ✅ 음식 선택 시 자동 입력 */
    function selectFood(food) {
        isManualEntry = false;
        console.log("✅ 선택된 음식 데이터:", food);

        document.getElementById("selectedFoodName").value = food.foodName;
        document.getElementById("standardWeight").value = `표준중량: ${food.standardWeight}`;
        document.getElementById("userWeight").value = "";
        document.getElementById("foodCalories").value = food.calories;
        document.getElementById("foodCarbs").value = food.carbs;
        document.getElementById("foodProtein").value = food.protein;
        document.getElementById("foodFat").value = food.fat;

        let userWeightInput = document.getElementById("userWeight");
        userWeightInput.value = "";
        userWeightInput.focus();

        userWeightInput.removeEventListener("input", handleUserWeightInput);
        userWeightInput.addEventListener("input", function () {
            if (!isManualEntry) {  // 직접 입력 모드가 아니면 자동 계산 실행
                handleUserWeightInput(food);
            }
        });

        setReadOnly(true);
    }

    /**  먹은 g 입력 시 자동으로 영양소 업데이트 */
    function handleUserWeightInput(food) {
        let userWeight = parseFloat(document.getElementById("userWeight").value) || 0;

        if (!userWeight || userWeight <= 0) {
            return;  // g 수가 입력되지 않았거나 0 이하이면 계산하지 않음
        }
        updateNutritionalValues(food, userWeight);
    }

    // 영양소 계산 
    function updateNutritionalValues(food, gram) {
        gram = parseFloat(gram) || 0;

        document.getElementById("foodCalories").value = (parseFloat(food.calories) * gram).toFixed(1);
        document.getElementById("foodCarbs").value = (parseFloat(food.carbs) * gram).toFixed(1);
        document.getElementById("foodProtein").value = (parseFloat(food.protein) * gram).toFixed(1);
        document.getElementById("foodFat").value = (parseFloat(food.fat) * gram).toFixed(1);
    }

    //입력 필드 초기화 함수 (직접 입력 모드 변경 시)
    function resetFoodInputFields() {
        document.getElementById("selectedFoodName").value = "";
        document.getElementById("foodCalories").value = "";
        document.getElementById("foodCarbs").value = "";
        document.getElementById("foodProtein").value = "";
        document.getElementById("foodFat").value = "";
        document.getElementById("standardWeight").value = "";
        document.getElementById("userWeight").value = "";
    }

    let selectedFoods = []; // 사용자가 추가한 음식 목록

    /** 음식 추가 버튼 클릭 시 실행 */
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
            listItem.dataset.foodName = foodName;
            listItem.dataset.calories = foodCalories;
            listItem.dataset.carbs = foodCarbs;
            listItem.dataset.protein = foodProtein;
            listItem.dataset.fat = foodFat;

            listItem.innerHTML = `
                <span>${foodName} (${userWeight}g, ${foodCalories} kcal)</span>
                <button class="btn btn-danger btn-sm remove-food">삭제</button>
            `;

            // 삭제 버튼 이벤트 추가
            listItem.querySelector(".remove-food").addEventListener("click", function () {
                foodList.removeChild(listItem);
                updateTotalNutrition();
                updateMealFoodList();
            });

            foodList.appendChild(listItem);

            // 총 영양소 업데이트
            updateTotalNutrition();

            // 포함된 음식 업데이트 
            updateMealFoodList();

            // ✅ 검색 모달 닫기
            let foodModalElement = document.getElementById("foodModal");
            let foodModal = bootstrap.Modal.getInstance(foodModalElement) || new bootstrap.Modal(foodModalElement);

            foodModal.hide(); // 🔥 검색 모달을 닫는다

            // ✅ 기존 이벤트 리스너 제거 (중복 실행 방지)
            foodModalElement.removeEventListener("hidden.bs.modal", openMealModal);

            // ✅ 검색 모달이 닫힌 후 실행될 이벤트 등록
            function openMealModal() {
                let mealModalElement = document.getElementById("mealModal");
                let mealModal = bootstrap.Modal.getInstance(mealModalElement) || new bootstrap.Modal(mealModalElement);
                mealModal.show();

                foodModalElement.removeEventListener("hidden.bs.modal", openMealModal);
            }

            foodModalElement.addEventListener("hidden.bs.modal", openMealModal);


            // 음식 추가 후 검색창 초기화
            document.getElementById("foodSearch").value = "";
            document.getElementById("foodResults").innerHTML = "";
            document.getElementById("selectedFoodName").value = "";
            document.getElementById("foodCalories").value = "";
            document.getElementById("foodCarbs").value = "";
            document.getElementById("foodProtein").value = "";
            document.getElementById("foodFat").value = "";
            document.getElementById("standardWeight").value = "";
            document.getElementById("userweight").value = "";

        });
    }

    /**포함된 음식 리스트 업데이트 */
    function updateMealFoodList() {
        let mealFoodList = document.querySelectorAll(".meal-food-list");
        let selectedFoods = document.querySelectorAll("#selectedFoodsList li");

        //포함된 음식 리스트 비우기
        mealFoodList.forEach(list => list.innerHTML = "");

        // 추가된 음식 목록에서 음식 이름 가져와서 포함된 음식에 추가
        selectedFoods.forEach(foodItem => {
            let foodName = foodItem.dataset.foodName;
            mealFoodList.forEach(list => {
                let li = document.createElement("li");
                li.textContent = foodName;
                list.appendChild(li);
            });
        });
    }

    /**  총 영양소 합산 */
    function updateTotalNutrition() {
        let totalCalories = 0, totalCarbs = 0, totalProtein = 0, totalFat = 0;

        document.querySelectorAll("#selectedFoodsList li").forEach(foodItem => {
            totalCalories += parseFloat(foodItem.dataset.calories) || 0;
            totalCarbs += parseFloat(foodItem.dataset.carbs) || 0;
            totalProtein += parseFloat(foodItem.dataset.protein) || 0;
            totalFat += parseFloat(foodItem.dataset.fat) || 0;
        });

        // 새 식단 추가 모달의 입력 필드에 값 반영
        document.getElementById("mealCalories").value = totalCalories.toFixed(1);
        document.getElementById("mealCarbs").value = totalCarbs.toFixed(1);
        document.getElementById("mealProtein").value = totalProtein.toFixed(1);
        document.getElementById("mealFat").value = totalFat.toFixed(1);
    }

    // 음식 추가 후 검색어 지우기 (이벤트 중복 방지)
    document.getElementById("foodSearch").addEventListener("input", function () {
        document.getElementById("foodResults").innerHTML = "";
    });

});
