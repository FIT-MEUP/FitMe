package fitmeup.entity;

import java.time.LocalDateTime;

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
@Table(name="pt_session_history")
public class PTSessionHistoryEntity {
    private int historyId;
    private int userId;
    private String changeType;
    private int changeAmount;
    private LocalDateTime changeDate;
    private String reason;
    
}
