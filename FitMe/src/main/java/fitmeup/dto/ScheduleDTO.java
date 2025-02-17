package fitmeup.dto;

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
public class ScheduleDTO {
    private int scheduleId;
    private int trainerId;
    private int userId;
    private String status;
    private String attendanceStatus;
    private boolean sessionDeducted;
    
}
