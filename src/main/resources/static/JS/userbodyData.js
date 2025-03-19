document.addEventListener("DOMContentLoaded", function() {
    document.querySelectorAll(".delete-btn").forEach(button => {
        button.addEventListener("click", function() {
            let dataId = this.getAttribute("data-id");
            confirmDelete(dataId, this);
        });
    });
});

function confirmDelete(dataId, button) {
    if (confirm("정말 삭제하시겠습니까?")) {
        $.ajax({
            type: "POST",
            url: "/user/deleteHealthData",
            data: { dataId: dataId },
            success: function(response) {
                if (response === "OK") {
                    alert("삭제 완료!");
                    let row = button.closest("tr");
                    if (row) {
                        row.remove();
                    }
                } else {
                    alert("삭제 실패! 다시 시도해주세요.");
                }
            },
            error: function() {
                alert("삭제 요청에 실패했습니다.");
            }
        });
    }
}

function editRow(button) {
    let row = button.closest('tr');
    let cells = row.querySelectorAll('td');
    let dataId = row.id.replace("row-", "");
    
    let values = [...cells].map(cell => cell.innerText.replace(/[^0-9.]/g, ''));
    let date = cells[6].innerText;
    
    row.innerHTML = `
        <td><input type="number" value="${values[0]}" id="height-${dataId}" /></td>
        <td><input type="number" value="${values[1]}" id="weight-${dataId}" /></td>
        <td><input type="number" step="0.1" value="${values[2]}" id="bmi-${dataId}" /></td>
        <td><input type="number" step="0.1" value="${values[3]}" id="fatMass-${dataId}" /></td>
        <td><input type="number" step="0.1" value="${values[4]}" id="muscleMass-${dataId}" /></td>
        <td><input type="number" value="${values[5]}" id="bmr-${dataId}" /></td>
        <td><input type="date" value="${date}" id="date-${dataId}" /></td>
        <td><button onclick="saveRow(${dataId})" class="btn btn-success">저장</button></td>
        <td><button onclick="cancelEdit(${dataId})" class="btn btn-secondary">취소</button></td>
    `;
}

function saveRow(dataId) {
    let row = document.getElementById(`row-${dataId}`);
    let values = {
        height: document.getElementById(`height-${dataId}`).value,
        weight: document.getElementById(`weight-${dataId}`).value,
        bmi: document.getElementById(`bmi-${dataId}`).value,
        fatMass: document.getElementById(`fatMass-${dataId}`).value,
        muscleMass: document.getElementById(`muscleMass-${dataId}`).value,
        bmr: document.getElementById(`bmr-${dataId}`).value,
        recordDate: document.getElementById(`date-${dataId}`).value
    };

    $.ajax({
        type: "POST",
        url: "/user/updateHealthData",
        data: { dataId, ...values },
        success: function(response) {
            if (response === "OK") {
                let formattedDate = new Date(values.recordDate).toISOString().split('T')[0];
                row.innerHTML = `
                    <td>${values.height} cm</td>
                    <td>${values.weight} kg</td>
                    <td>${values.bmi}</td>
                    <td>${values.fatMass}%</td>
                    <td>${values.muscleMass} kg</td>
                    <td>${values.bmr} kcal</td>
                    <td>${formattedDate}</td>
                    <td><button onclick="editRow(this)" class="btn-warning">수정</button></td>
                    <td><button class="btn-danger delete-btn" data-id="${dataId}">삭제</button></td>
                `;
            } else {
                alert("수정 실패! 다시 시도해주세요.");
            }
        },
        error: function() {
            alert("수정 요청 실패");
        }
    });
}

function cancelEdit(dataId) {
    location.reload();
}
