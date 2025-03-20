package fitmeup.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import fitmeup.entity.HealthDataEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

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
	    private LocalDate recordDate;
//	    private Long changeAmount;
	    
				public static HealthDataDTO toDTO(HealthDataEntity entity) {
					return HealthDataDTO.builder()
							.dataId(entity.getDataId())
							.userId(entity.getUser().getUserId())
//							.userId(entity.getUserId())
							.height(entity.getHeight())
							.weight(entity.getWeight())
							.muscleMass(entity.getMuscleMass())
							.fatMass(entity.getFatMass())
							.bmi(entity.getBmi())
							.basalMetabolicRate(entity.getBasalMetabolicRate())
							.recordDate(entity.getRecordDate())
							.build();
				}
	}


