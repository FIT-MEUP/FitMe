package fitmeup.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fitmeup.dto.ScheduleDTO;
import fitmeup.dto.TrainerScheduleDTO;
import fitmeup.entity.PtSessionHistoryEntity;
import fitmeup.entity.ScheduleEntity;
import fitmeup.entity.TrainerEntity;
import fitmeup.entity.TrainerScheduleEntity;
import fitmeup.entity.UserEntity;
import fitmeup.repository.PtSessionHistoryRepository;
import fitmeup.repository.ScheduleRepository;
import fitmeup.repository.TrainerApplicationRepository;
import fitmeup.repository.TrainerRepository;
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
	private final TrainerRepository trainerRepository;
	private final PtSessionHistoryRepository ptSessionHistoryRepository;
	
	//trainerSchedule Read
	public List<TrainerScheduleDTO> selectTrainerScheduleAll(Long userId){
		Optional<TrainerEntity> trainer = trainerRepository.findByUser_UserId(userId);
		Long trainerId = trainer.get().getTrainerId();
		
		List<TrainerScheduleEntity> temp= trainerScheduleRepository.findByTrainerTrainerId(trainerId);
		List<TrainerScheduleDTO> list = new ArrayList<>();

		temp.forEach((entity) -> list.add(TrainerScheduleDTO.toDTO(entity)));
		
		return list;	
	}
	
	
	public long findTrainerIdbyUserId(Long userId) {
		Optional<TrainerEntity> trainer = trainerRepository.findByUser_UserId(userId);
		return trainer.get().getTrainerId();
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
		    log.info("트레이너 아이디: ",
		    		trainerApplicationRepository.findByUserUserId(userId)
		           .map(app -> app.getTrainer().getTrainerId())
		           .orElse(0L));
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
		 
		 public Long findTrainerUserId(Long trainerId) {
			 Optional<TrainerEntity> temp = trainerRepository.findById(trainerId);
			 
			 return temp.get().getUser().getUserId(); 
		 }
		 
		 public PtSessionHistoryEntity selectfirstByUserId(Long userId) {
			 List<PtSessionHistoryEntity> historyList =ptSessionHistoryRepository.findByUserUserId(userId, Sort.by("changeDate").descending());
			 PtSessionHistoryEntity latestHistory = historyList.isEmpty() ? null : historyList.get(0);
			 return latestHistory;
		 }
		 //trainer의 userId를 통해 그에 해당하는 schedule을 List를 뽑은후 지금 시간에서 10분전부터 그 시각까지 
		 //pt시작 버튼을 누르면 없어지는 형태
		 public String minusChangeAmount(Long userId) {
			// Trainer의 userId로 schedule 목록을 가져옵니다.
			    List<ScheduleEntity> scheduleList = scheduleRepository.findByTrainerTrainerId(userId);
			    log.info("Retrieved schedules: {}", scheduleList.size());

			    // 현재 시간과 10분 후 시간 계산
			    LocalDateTime now = LocalDateTime.now();
			    LocalDateTime tenMinutesLater = now.plusMinutes(10);
			  
			    // scheduleList에서 startTime이 [now, tenMinutesLater) 범위에 있는지 확인
			    log.info("현재 시각: {}", now);
			    log.info("10분 후 시각: {}", tenMinutesLater);
			    boolean exists = scheduleList.stream()
			            .anyMatch(schedule -> {
			            	 
			                LocalDateTime startTime = schedule.getStartTime();
			                log.info("schedule startTime: {}", schedule.getStartTime());
			                return ( !startTime.isBefore(now) ) && startTime.isBefore(tenMinutesLater);
			            });
			   
			    log.info(exists ? "success" : "false");
			    return exists ? "success" : "false";
			
		 }	

}
