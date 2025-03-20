document.addEventListener("DOMContentLoaded", function () {
    /** ✅ 수정 모달 열기 */
    document.querySelectorAll(".edit-meal-btn").forEach(button => {
        button.addEventListener("click", function () {
            let mealId = this.getAttribute("data-mealid");
            let mealDate = this.getAttribute("data-mealdate");

            if (!mealId) {
                console.error("❌ mealId가 정의되지 않음!");
                return;
            }

            // 수정 모달에 데이터 채우기
            document.getElementById("editMealId").value = mealId;
            document.getElementById("editMealDate").value = mealDate;

            // ✅ Fetch 요청 (식단 정보 가져오기)
            fetch(`/meals/${mealId}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`서버 오류: ${response.status}`);
                    }
                    return response.json();
                })
                .then(meal => {
                    if (!meal) {
                        console.error("❌ meal 데이터가 없음!");
                        return;
                    }

                    document.getElementById("editMealType").value = meal.mealType || "아침";
                    document.getElementById("editMealCalories").value = meal.totalCalories || 0;
                    document.getElementById("editMealCarbs").value = meal.totalCarbs || 0;
                    document.getElementById("editMealProtein").value = meal.totalProtein || 0;
                    document.getElementById("editMealFat").value = meal.totalFat || 0;

                    let foodList = document.getElementById("editSelectedFoodsList");
                    foodList.innerHTML = "";

                    (meal.foodList || []).forEach(food => {
                        let listItem = document.createElement("li");
                        listItem.className = "list-group-item d-flex justify-content-between align-items-center";
                        listItem.dataset.foodId = food.foodId;
                        listItem.dataset.calories = food.calories;
                        listItem.dataset.carbs = food.carbs;
                        listItem.dataset.protein = food.protein;
                        listItem.dataset.fat = food.fat;

                        listItem.innerHTML = `${food.foodName} (${food.calories} kcal)
                            <button class="btn btn-danger btn-sm remove-food">삭제</button>`;

                        listItem.querySelector(".remove-food").addEventListener("click", function () {
                            foodList.removeChild(listItem);
                            updateTotalNutrition();
                        });

                        foodList.appendChild(listItem);
                    });

                    setTimeout(() => updateTotalNutrition("edit"), 100);
                })
                .catch(error => console.error("❌ meal 데이터 불러오기 실패:", error));

            let editMealModal = new bootstrap.Modal(document.getElementById("editMealModal"));
            editMealModal.show();
        });
    });

    /** ✅ 수정 모달에서 "음식 추가" 버튼 클릭 시 editFoodModal 열기 */
    document.querySelectorAll("#editMealModal button[data-bs-target='#editFoodModal']").forEach(button => {
        button.addEventListener("click", function () {
            let editFoodModalElement = document.getElementById("editFoodModal");
            if (editFoodModalElement) {
                let editFoodModal = new bootstrap.Modal(editFoodModalElement);
                editFoodModal.show();
            } else {
                console.error("❌ editFoodModal이 존재하지 않음!");
            }
        });
    });

    /** ✅ 수정 모달에서 음식 검색 기능 */
    document.getElementById("editFoodSearch").addEventListener("input", function () {
        let query = this.value.trim();
        if (query.length < 1) {
            document.getElementById("editFoodResults").innerHTML = "";
            return;
        }

        fetch(`/foodsearch?query=${query}`)
            .then(response => response.json())
            .then(data => {
                let resultsDiv = document.getElementById("editFoodResults");
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
                        selectEditFood(food);
                    };
                    resultsDiv.appendChild(btn);
                });
            })
            .catch(error => console.error("❌ 음식 검색 중 오류 발생:", error));
    });

    /** ✅ 수정 모달에서 총 영양소 합산 */
    function updateTotalNutrition(mode = "edit") {
        let totalCalories = 0, totalCarbs = 0, totalProtein = 0, totalFat = 0;
        let foodList = document.querySelectorAll(`#${mode}SelectedFoodsList li`);

        foodList.forEach(foodItem => {
            totalCalories += parseFloat(foodItem.dataset.calories) || 0;
            totalCarbs += parseFloat(foodItem.dataset.carbs) || 0;
            totalProtein += parseFloat(foodItem.dataset.protein) || 0;
            totalFat += parseFloat(foodItem.dataset.fat) || 0;
        });

        document.getElementById(`${mode}MealCalories`).value = totalCalories.toFixed(1);
        document.getElementById(`${mode}MealCarbs`).value = totalCarbs.toFixed(1);
        document.getElementById(`${mode}MealProtein`).value = totalProtein.toFixed(1);
        document.getElementById(`${mode}MealFat`).value = totalFat.toFixed(1);
    }

    /** ✅ 수정 모달에서 음식 선택 시 자동 입력 */
    function selectEditFood(food) {
        document.getElementById("editSelectedFoodName").value = food.foodName;
        document.getElementById("editStandardWeight").value = `표준중량: ${food.standardWeight}`;
        document.getElementById("editUserWeight").value = "";
        document.getElementById("editFoodCalories").value = food.calories;
        document.getElementById("editFoodCarbs").value = food.carbs;
        document.getElementById("editFoodProtein").value = food.protein;
        document.getElementById("editFoodFat").value = food.fat;

        let userWeightInput = document.getElementById("editUserWeight");
        userWeightInput.value = "";
        userWeightInput.focus();

        userWeightInput.removeEventListener("input", handleEditUserWeightInput);
        userWeightInput.addEventListener("input", function () {
            handleEditUserWeightInput(food);
        });

        updateTotalNutrition("edit");
    }

    /** ✅ 수정 모달에서 영양소 계산 */
    function handleEditUserWeightInput(food) {
        let userWeight = parseFloat(document.getElementById("editUserWeight").value) || 0;
        if (!userWeight || userWeight <= 0) {
            return;
        }
        updateEditNutritionalValues(food, userWeight);
        updateTotalNutrition("edit");
    }

    function updateEditNutritionalValues(food, gram) {
        gram = parseFloat(gram) || 0;
        document.getElementById("editFoodCalories").value = (parseFloat(food.calories) * gram).toFixed(1);
        document.getElementById("editFoodCarbs").value = (parseFloat(food.carbs) * gram).toFixed(1);
        document.getElementById("editFoodProtein").value = (parseFloat(food.protein) * gram).toFixed(1);
        document.getElementById("editFoodFat").value = (parseFloat(food.fat) * gram).toFixed(1);
    }

    /** ✅ 직접 입력 버튼 클릭 시, 사용자가 직접 음식 입력 가능 */
    document.getElementById("enableEditManualEntry").addEventListener("click", function () {
        isManualEntry = true;
        resetEditFoodInputFields();
        setEditReadOnly(false); // 🔹 직접 입력 모드에서는 입력 가능하게 변경

        let userWeightInput = document.getElementById("editUserWeight");
        if (userWeightInput) {
            userWeightInput.removeEventListener("input", handleEditUserWeightInput);
        }
    });
    // 🔹 readonly 속성을 설정/해제하는 함수
    function setEditReadOnly(state) {
        let inputs = ["editFoodCalories", "editFoodCarbs", "editFoodProtein", "editFoodFat"];
        let foodNameInput = document.getElementById("editSelectedFoodName");

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

        if (foodNameInput) {
            if (state) {
                foodNameInput.setAttribute("readonly", true);
            } else {
                foodNameInput.removeAttribute("readonly");
            }
        }
    }
    document.getElementById("addEditFoodButton").addEventListener("click", function () {
        let foodName = document.getElementById("editSelectedFoodName").value;
        let userWeight = parseFloat(document.getElementById("editUserWeight").value) || 0;
        let foodCalories = parseFloat(document.getElementById("editFoodCalories").value);
        let foodCarbs = parseFloat(document.getElementById("editFoodCarbs").value);
        let foodProtein = parseFloat(document.getElementById("editFoodProtein").value);
        let foodFat = parseFloat(document.getElementById("editFoodFat").value);

        if (!foodName || userWeight < 1) {
            alert("음식을 선택하거나, 음식을 직접 입력하고 먹은 g 수를 입력하세요!");
            return;
        }

        let foodList = document.getElementById("editSelectedFoodsList");
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

        listItem.querySelector(".remove-food").addEventListener("click", function () {
            foodList.removeChild(listItem);
            updateTotalNutrition("edit");
        });

        foodList.appendChild(listItem);

        // ✅ 총 영양소 업데이트
        updateTotalNutrition("edit");

        // ✅ 음식 추가 후 검색 모달 닫기 & 원래 수정 모달 열기
        let editFoodModalElement = document.getElementById("editFoodModal");
        let editFoodModal = bootstrap.Modal.getInstance(editFoodModalElement) || new bootstrap.Modal(editFoodModalElement);
        editFoodModal.hide();

        let editMealModalElement = document.getElementById("editMealModal");
        let editMealModal = bootstrap.Modal.getInstance(editMealModalElement) || new bootstrap.Modal(editMealModalElement);
        editMealModal.show();

        // ✅ 입력 필드 초기화
        resetEditFoodInputFields();
    });

    /** ✅ 입력 필드 초기화 함수 (직접 입력 모드 변경 시) */
    function resetEditFoodInputFields() {
        document.getElementById("editSelectedFoodName").value = "";
        document.getElementById("editFoodCalories").value = "";
        document.getElementById("editFoodCarbs").value = "";
        document.getElementById("editFoodProtein").value = "";
        document.getElementById("editFoodFat").value = "";
        document.getElementById("editStandardWeight").value = "";
        document.getElementById("editUserWeight").value = "";
    }

});
