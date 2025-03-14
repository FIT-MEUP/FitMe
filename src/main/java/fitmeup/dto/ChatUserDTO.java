package fitmeup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatUserDTO {
  private Long userId;    // 상대방 사용자 이름
  private int unreadCount;    // 해당 사용자와의 대화에서 읽지 않은 메시지 수
  private boolean online;     // 온라인 상태 (true이면 초록색 점 표시 등 UI 처리)
}
