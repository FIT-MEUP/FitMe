// 트레이너 목록 데이터 (백엔드에서 가져올 수도 있음)
const trainers = [
    { id: 1, name: "김철수", profileImg: "trainer1.jpg" },
    { id: 2, name: "이영희", profileImg: "trainer2.jpg" },
    { id: 3, name: "박민수", profileImg: "trainer3.jpg" }
];

// 트레이너 목록을 동적으로 생성
const trainerContainer = document.getElementById("trainer-container");

trainers.forEach(trainer => {
    const card = document.createElement("div");
    card.classList.add("card");

    card.innerHTML = `
        <div class="image">
            <img src="${trainer.profileImg}" alt="${trainer.name}" style="width: 100%; height: 100%; object-fit: cover; border-radius: 10px;">
        </div>
        <div class="label">${trainer.name}</div>
    `;

    // 클릭 시 프로필 페이지로 이동
    card.addEventListener("click", () => {
        window.location.href = `profile.html?id=${trainer.id}`;
    });

    trainerContainer.appendChild(card);
});
