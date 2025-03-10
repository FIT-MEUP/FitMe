package fitmeup.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fitmeup.dto.ScheduleDTO;
import fitmeup.dto.TrainerScheduleDTO;
import fitmeup.entity.ScheduleEntity;
import fitmeup.entity.TrainerScheduleEntity;
import fitmeup.entity.UserEntity;
import fitmeup.repository.ScheduleRepository;
import fitmeup.repository.TrainerApplicationRepository;
import fitmeup.repository.TrainerScheduleRepository;
import fitmeup.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

	private final ScheduleRepository scheduleRepository;
	private final TrainerScheduleRepository trainerScheduleRepository;
	private final TrainerApplicationRepository trainerApplicationRepository;
	private final UserRepository userRepository;

	
	//trainerSchedule Read
	public List<TrainerScheduleDTO> selectTrainerScheduleAll(Long trainerId){
		
		List<TrainerScheduleEntity> temp= trainerScheduleRepository.findByTrainerTrainerId(trainerId);
		List<TrainerScheduleDTO> list = new ArrayList<>();

		temp.forEach((entity) -> list.add(TrainerScheduleDTO.toDTO(entity)));
		
		return list;	
	}
	
	//trainerSchedule Create
	public void insertTrainerSchedule(TrainerScheduleDTO trainerScheduleDTO) {
		
		
		TrainerScheduleEntity trainerScheduleEntity = TrainerScheduleEntity.toEntity(trainerScheduleDTO);
		
		trainerScheduleRepository.save(trainerScheduleEntity);
	}
	
	//TrainerSchedule Delete
	@Transactional
	public void deleteTrainerSchedule(Integer trainerScheduleId) {
		Optional<TrainerScheduleEntity> temp = trainerScheduleRepository.findById(trainerScheduleId);
		
		if(!temp.isPresent()) return ;
		
		trainerScheduleRepository.deleteById(trainerScheduleId);
	}
	
	
	//ScheduleRead
	public List<ScheduleDTO> selectAll(Long trainerId){

	List<ScheduleEntity> temp = scheduleRepository.findByTrainerTrainerId(trainerId);

	List<ScheduleDTO> list = new ArrayList<>();

	temp.forEach((entity) -> list.add(ScheduleDTO.toDTO(entity)));

	return list;
}
	
	//ScheduleCreate
	public void insertSchedule(ScheduleDTO trainerScheduleId) {
		
		
		ScheduleEntity scheduleEntity = ScheduleEntity.toEntity(trainerScheduleId);
		
		scheduleRepository.save(scheduleEntity);
	}

	
	//Schedule Delete
		@Transactional
		public void deleteSchedule(Integer ScheduleId) {
			Optional<ScheduleEntity> temp = scheduleRepository.findById(ScheduleId);
			
			if(!temp.isPresent()) return ;
			
			scheduleRepository.deleteById(ScheduleId);
		}
		
		

		
		public long findTrainerId(Long userId) {
		    return trainerApplicationRepository.findByUserUserId(userId)
		           .map(app -> app.getTrainer().getTrainerId())
		           .orElse(0L);
		}
		
		//자신의 이름 검색
		 public String findUserName(Long userId) {
		        return userRepository.findById(userId)
		                .map(UserEntity::getUserName)
		                .orElse("Unknown User"); // 해당 사용자가 없을 경우 반환할 기본값
		    }
	
		 
		// userId로 예약된 스케줄들을 DTO 목록으로 반환하는 메소드 추가
		 public List<ScheduleDTO> selectAllByUserId(Long userId) {
		     List<ScheduleEntity> entities = scheduleRepository.findByUserUserId(userId);
		     return entities.stream()
		                    .map(ScheduleDTO::toDTO)
		                    .collect(Collectors.toList());
		 }

	

}