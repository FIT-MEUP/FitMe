// 회원 정보 데이터
const users = {
    A: { 이름: "김철수", 나이: 25, 성별: "남", 키: "178cm", 체중: "75kg" },
    B: { 이름: "이영희", 나이: 22, 성별: "여", 키: "165cm", 체중: "55kg" },
    C: { 이름: "박민수", 나이: 30, 성별: "남", 키: "182cm", 체중: "85kg" },
    D: { 이름: "정수진", 나이: 27, 성별: "여", 키: "170cm", 체중: "60kg" },
    E: { 이름: "최준호", 나이: 23, 성별: "남", 키: "175cm", 체중: "70kg" }
};

// 회원 정보 표시 함수
function showUserInfo(userKey) {
    const user = users[userKey];
    const userInfoDiv = document.getElementById("userInfo");

    userInfoDiv.innerHTML = `
        <h3>${user.이름}</h3>
        <table class="user-table">
            <tr>
                <th>나이</th><th>성별</th>
            </tr>
            <tr>
                <td>${user.나이}세</td><td>${user.성별}</td>
            </tr>
            <tr>
                <th>키</th><th>체중</th><th></th>
            </tr>
            <tr>
                <td>${user.키}</td><td>${user.체중}</td><td></td>
            </tr>
        </table>
    `;
}
