package fitmeup.dto;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;

import fitmeup.entity.HealthDataEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder

public class HealthDataDTO {
	 private Long dataId;
	    private Long userId;
	    private BigDecimal height;
	    private BigDecimal weight;
	    private BigDecimal muscleMass;
	    private BigDecimal fatMass;
	    private BigDecimal bmi;
	    private BigDecimal basalMetabolicRate;
	    private LocalDateTime recordDate;

	    
				public static HealthDataDTO toDTO(HealthDataEntity entity) {
					return HealthDataDTO.builder()
							.dataId(entity.getDataId())
//							.userId(entity.getUser().getUserId())
							.userId(entity.getUserId())
							.height(entity.getHeight())
							.weight(entity.getWeight())
							.muscleMass(entity.getMuscleMass())
							.bmi(entity.getBmi())
							.basalMetabolicRate(entity.getBasalMetabolicRate())
							.build();
				}
	}


