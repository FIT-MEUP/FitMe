package fitmeup.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import fitmeup.dto.HealthDataDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "health_data")

public class HealthDataEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "data_id")
    private Long dataId; //
	
//	@OneToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private UserEntity user; // 트레이너는 User와 연결됨
//	@Column(name="user_id")
//    private Long userId; // 트레이너는 User와 연결됨
	
	@Column(name="user_id")
	private Long userId;
	
	@Column(nullable = false, name="weight", precision = 5, scale = 2)
    private BigDecimal weight;
	
	@Column(nullable = false, name="muscle_mass", precision = 5, scale = 2)
    private BigDecimal muscleMass;
	
	@Column(nullable = false, name="fat_mass", precision = 5, scale = 2)
    private BigDecimal fatMass;
	
	@Column(nullable = false,name="height", precision = 5, scale = 2)
    private BigDecimal height;
	
	@Column(nullable = false,name="bmi", precision = 5, scale = 2)
    private BigDecimal bmi;
	
	@Column(nullable = false, name="basal_metabolic_rate", precision = 6, scale = 2)
    private BigDecimal basalMetabolicRate;
	
	@Column(nullable = false,name="record_date", updatable = true)
    private LocalDate recordDate ;
    		@PrePersist
    		protected void onCreate() {
    		    if (this.recordDate == null) {
    		        this.recordDate = LocalDate.now();
    		    }
    		}

	
	public static HealthDataEntity toEntity(HealthDataDTO healthDTO) {
		HealthDataEntity entity = HealthDataEntity.builder()
				.dataId(healthDTO.getDataId())
//				.user(user)
				.userId(healthDTO.getUserId())
				.height(healthDTO.getHeight())
				.weight(healthDTO.getWeight())
				.muscleMass(healthDTO.getMuscleMass())
				.fatMass(healthDTO.getFatMass())
				.bmi(healthDTO.getBmi())
				.basalMetabolicRate(healthDTO.getBasalMetabolicRate())
				.recordDate(healthDTO.getRecordDate())
				.build();
		return entity;
	
	}
	


}
