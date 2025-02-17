package fitmeup.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Entity
@Table(name="schedule")
public class ScheduleEntity {
    private int scheduleId;
    private int trainerId;
    private int userId;
    private String status;
    private String attendanceStatus;
    private boolean sessionDeducted;
    
}
