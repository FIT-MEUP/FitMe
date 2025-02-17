package fitmeup.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class PTSessionHistoryDTO {
    private int historyId;
    private int userId;
    private String changeType;
    private int changeAmount;
    private LocalDateTime changeDate;
    private String reason;

    // 생성자, getter 및 setter 메서드
}
