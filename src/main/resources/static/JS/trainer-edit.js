// trainer-edit.js

/**
 * 사진 삭제 함수
 * @param {string} photoId - 삭제할 사진의 ID
 */
function deletePhoto(photoId) {
  if (!confirm("정말 삭제하시겠습니까?")) return;
  fetch('/api/trainer/photo/delete', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({ "photoId": photoId })
  })
  .then(response => {
    if (response.ok) {
      alert("사진이 삭제되었습니다.");
      window.location.reload();
    } else {
      alert("사진 삭제에 실패했습니다.");
    }
  })
  .catch(error => {
    console.error("사진 삭제 오류:", error);
    alert("오류가 발생했습니다.");
  });
}
