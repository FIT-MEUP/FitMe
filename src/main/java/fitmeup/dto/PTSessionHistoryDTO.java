package fitmeup.dto;

import java.time.LocalDateTime;

import fitmeup.entity.PTSessionHistoryEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PTSessionHistoryDTO {
	   private Long historyId;
	    private Long userId;               // UserEntity의 userId 참조
	    private String changeType;         // "Added" 또는 "Deducted"
	    private Long changeAmount;         // 변경된 PT 세션 수
	    private LocalDateTime changeDate;  // 변경 날짜
	    private String reason;             // 변경 사유

	    // Entity → DTO 변환 메서드 (필요시)
	    public static PTSessionHistoryDTO fromEntity(PTSessionHistoryEntity entity) {
	        return PTSessionHistoryDTO.builder()
	                .historyId(entity.getHistoryId())
	                .userId(entity.getUser().getUserId())
	                .changeType(entity.getChangeType().name())
	                .changeAmount(entity.getChangeAmount())
	                .changeDate(entity.getChangeDate())
	                .reason(entity.getReason())
	                .build();
	    }
}
