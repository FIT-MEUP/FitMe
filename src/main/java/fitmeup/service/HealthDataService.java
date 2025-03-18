package fitmeup.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import fitmeup.dto.HealthDataDTO;
import fitmeup.entity.HealthDataEntity;
import fitmeup.entity.UserEntity;
import fitmeup.repository.HealthDataRepository;
import fitmeup.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthDataService {
	private final HealthDataRepository healthRepository;
	private final UserRepository userRepository;
	
	/**
	 * 신체 데이터 등록
	 * @param bookDTO
	 */
	public void insert(HealthDataDTO healthDTO) {
		HealthDataEntity entity = HealthDataEntity.toEntity(healthDTO);
		log.info("==============hihi{}}", entity);
		
		healthRepository.save(entity);
	}
	/**
	 * 등록한 신체 데이터 조회 (역순)
	 * @return
	 */

	
	  public List<HealthDataDTO> selectAll() { List<HealthDataEntity> temp =
	  healthRepository.findAll(Sort.by(Sort.Direction.DESC, "recordDate"));
	  List<HealthDataDTO> list = new ArrayList<>();
	  
	  temp.forEach((entity) -> list.add(HealthDataDTO.toDTO(entity)));
	  
	  return list; }
	  
	  public List <HealthDataDTO> listFindByUserId(Long userId){ List
	  <HealthDataEntity> temp = healthRepository.findByUserId(userId,
	  Sort.by(Sort.Direction.DESC, "recordDate"));
	  
	  List<HealthDataDTO> list = new ArrayList<>();
	  
	  temp.forEach((entity) -> list.add(HealthDataDTO.toDTO(entity))); return list;
	  }
	 
	
	
	/**
	 * 등록한 신체 데이터 db 및 페이지에서 수정 & 삭제
	 * @return
	 */
	
	  @Transactional
	public void update(HealthDataDTO healthDTO) {
	    Optional<HealthDataEntity> optionalEntity = healthRepository.findById(healthDTO.getDataId());
	    if (optionalEntity.isPresent()) {
	        HealthDataEntity entity = optionalEntity.get();
	        entity.setHeight(healthDTO.getHeight());
	        entity.setWeight(healthDTO.getWeight());
	        entity.setMuscleMass(healthDTO.getMuscleMass());
	        entity.setBmi(healthDTO.getBmi());
	       // entity.setFatMass(healthDTO.getFatMass());
	        entity.setBasalMetabolicRate(healthDTO.getBasalMetabolicRate());
	        entity.setRecordDate(healthDTO.getRecordDate());
	        
	        healthRepository.save(entity);
	    }
	}

	public void delete(Long dataId) {
		
	    healthRepository.deleteById(dataId);
	}
	
	public HealthDataDTO getLatestHealthData(Long userId) {
	    List<HealthDataEntity> dataList = healthRepository.findByUserId(
	        userId, Sort.by(Sort.Direction.DESC, "recordDate"));
	    log.info("=======================getgetget{}",dataList.get(0).getUserId());
	    if (!dataList.isEmpty()) {
	        return HealthDataDTO.toDTO(dataList.get(0)); // 가장 최신 데이터 반환
	    }
	    
	    return null; // 데이터 없으면 null 반환

	}
	


	
//	  public void insert(HealthDataDTO healthDTO) {
//	        // userId를 이용해 UserEntity 가져오기
//	        UserEntity user = userRepository.findById(healthDTO.getUserId())
//	            .orElseThrow(() -> new IllegalArgumentException("User not found"));
//
//	        // UserEntity와 함께 엔티티 변환
//	        HealthDataEntity entity = HealthDataEntity.toEntity(healthDTO, user);
//	        log.info("entity : {}",entity);
//	        // 저장
//	        healthRepository.save(entity);
//	    }
	  /**
	     * 특정 사용자의 모든 건강 데이터 조회
	     */
//	    public List<HealthDataDTO> getAllHealthData(Long userId) {
//	        // 특정 userId에 해당하는 모든 건강 데이터 가져오기
//	        List<HealthDataEntity> entities = healthRepository.findByUser_UserId(userId);
//	        Optional<HealthDataEntity> entities = healthRepository.findById(userId);
//	        // HealthDataEntity -> HealthDataDTO 변환 후 반환
//	        return entities.stream()
//	                .map(HealthDataDTO::toDTO)
//	                .collect(Collectors.toList());
//	    }
//	  public List<HealthDataDTO> getAllHealthData(Long userId) {
//		    // userId에 해당하는 모든 건강 데이터를 조회
//		    List<HealthDataEntity> entities = healthRepository.findByUserId(userId);
//
//		    return entities.stream()
//		            .map(HealthDataDTO::toDTO)
//		            .collect(Collectors.toList());
//		}
	


}
