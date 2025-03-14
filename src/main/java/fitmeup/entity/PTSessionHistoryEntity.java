package fitmeup.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name="pt_session_history")
public class PtSessionHistoryEntity {
    
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "history_id")
	    private Long historyId;

	    // UserEntity와 다대일 관계 설정 (외래키 user_id)
	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "user_id", nullable = false)
	    private UserEntity user;

	    @Enumerated(EnumType.STRING)
	    @Column(name = "change_type", nullable = false)
		    private ChangeType changeType;
		
		    @Column(name = "change_amount", nullable = false)
		    private Long changeAmount;
		
		    @Column(name = "change_date", nullable = false)
		    private LocalDateTime changeDate;
		
		    @Column(name = "reason", nullable = false)
		    private String reason;
		
		    public enum ChangeType {
		        Added,   // PT 등록 등으로 횟수 증가
		        Deducted // 출석 등으로 횟수 차감
		    }

}
