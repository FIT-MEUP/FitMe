package fitmeup.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fitmeup.dto.PTSessionHistoryDTO;
import fitmeup.dto.ScheduleDTO;
import fitmeup.dto.TrainerScheduleDTO;
import fitmeup.entity.PTSessionHistoryEntity;
import fitmeup.entity.ScheduleEntity;
import fitmeup.entity.TrainerApplicationEntity;
import fitmeup.entity.TrainerEntity;
import fitmeup.entity.TrainerScheduleEntity;
import fitmeup.entity.UserEntity;
import fitmeup.repository.PTSessionHistoryRepository;
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
	private final PTSessionHistoryRepository ptSessionHistoryRepository;
		
	//trainerSchedule Read
	public List<TrainerScheduleDTO> selectTrainerScheduleAll(Long userId){
		Optional<TrainerEntity> trainer = trainerRepository.findByUser_UserId(userId);
		Long trainerId = trainer.get().getTrainerId();
		
		List<TrainerScheduleEntity> temp= trainerScheduleRepository.findByTrainerTrainerId(trainerId);
		List<TrainerScheduleDTO> list = new ArrayList<>();
		log.info("========================================= {}",temp.toString());
		
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
		
		

		
//		public Long findTrainerId(Long userId) {
////		    log.info("트레이너 아이디: ",
////		    		trainerApplicationRepository.findByUserUserId(userId)
////		           .map(app -> app.getTrainer().getTrainerId())
////		           .orElse(0L));
////			return trainerApplicationRepository.findByUserUserId(userId)
////		           .map(app -> app.getTrainer().getTrainerId())
////		           .orElse(0L);
//			Optional<TrainerApplicationEntity> applicationOpt = 
//                    trainerApplicationRepository.findByUserUserIdAndStatus(userId, TrainerApplicationEntity.Status.Approved);
//			return applicationOpt.get().getTrainer().getTrainerId();
//		    
//		}
		//에러 list로 받아서 에러 피하기
		public Long findTrainerId(Long userId) {
		    List<TrainerApplicationEntity> applications =
		        trainerApplicationRepository.findByUserUserIdAndStatus(userId, TrainerApplicationEntity.Status.Approved);
		    if(applications.isEmpty()){
		        return 0L; // 또는 예외 처리
		    }
		    // 여러 건일 경우 첫 번째 결과를 사용하거나, 추가 조건으로 정렬 후 선택
		    return applications.get(0).getTrainer().getTrainerId();
		}

		
		
		public Long findTrainertrainerId(Long userId) {
			Optional<TrainerEntity> temp=trainerRepository.findByUser_UserId(userId);
			return temp.get().getTrainerId();
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
		 public PTSessionHistoryEntity selectfirstByUserDTO(PTSessionHistoryDTO dto) {
			 List<PTSessionHistoryEntity> historyList =ptSessionHistoryRepository.findByUserUserId(dto.getUserId(), Sort.by("changeDate").descending());
			 if(historyList.isEmpty()) {
				 ptSessionHistoryRepository.save(PTSessionHistoryEntity.toEntity(dto));
			 }
			 PTSessionHistoryEntity latestHistory = historyList.isEmpty() ? null : historyList.get(0);
			 return latestHistory;
		 }
		 
		 public PTSessionHistoryEntity selectfirstByUserId(Long userId) {
			 List<PTSessionHistoryEntity> historyList =ptSessionHistoryRepository.findByUserUserId(userId, Sort.by("changeDate").descending());
			 PTSessionHistoryEntity latestHistory = historyList.isEmpty() ? null : historyList.get(0);
			 return latestHistory;
		 }
		 
		 //trainer의 userId를 통해 그에 해당하는 schedule을 List를 뽑은후 지금 시간에서 10분전부터 그 시각까지 
		 //pt시작 버튼을 누르면 없어지는 형태
		 public String minusChangeAmount(Long userId) {
			    // Trainer의 userId로 schedule 목록을 가져옵니다.
			 log.info("pt출석 trainer의 userId{}",userId);
			 	Optional <TrainerEntity> temp=trainerRepository.findByUser_UserId(userId);
			 	Long trainerId=temp.get().getTrainerId();
			    List<ScheduleEntity> scheduleList = scheduleRepository.findByTrainerTrainerId(trainerId);
			    log.info("Retrieved schedules: {}", scheduleList.size());

			    // 현재 시간과 10분 후 시간 계산
			    LocalDateTime now = LocalDateTime.now();
			    LocalDateTime tenMinutesLater = now.plusMinutes(10);
			  
			    log.info("현재 시각: {}", now);
			    log.info("10분 후 시각: {}", tenMinutesLater);

			    // scheduleList에서 startTime이 [now, tenMinutesLater) 범위에 있는 스케줄 찾기
			    Optional<ScheduleEntity> matchingSchedule = scheduleList.stream()
			            .filter(schedule -> {
			                LocalDateTime startTime = schedule.getStartTime();
			                log.info("schedule startTime: {}", startTime);
			                return (!startTime.isBefore(now)) && startTime.isBefore(tenMinutesLater);
			            })
			            .findFirst();

			    if (matchingSchedule.isPresent()) {
			        // 매칭된 스케줄의 UserEntity에서 userId 추출 (여기서 userId는 스케줄에 등록된 회원의 id)
			        Long matchedUserId = matchingSchedule.get().getUser().getUserId();
			        log.info("매칭된 스케줄의 userId: {}", matchedUserId);

			        // 해당 userId의 PT 세션 내역을 최신 순으로 조회 (changeDate 기준 내림차순)
			        List<PTSessionHistoryEntity> historyList = ptSessionHistoryRepository.findByUserUserId(matchedUserId, Sort.by("changeDate").descending());
			        PTSessionHistoryEntity latestHistory = historyList.get(0);  // 내역이 항상 있다고 가정

			        // 만약 최신 내역의 changeAmount가 0이면 더 이상 차감할 PT 세션이 없으므로 "noMore" 반환
			        if (latestHistory.getChangeAmount() == 0) {
			            log.info("PT 세션 남은 횟수가 0입니다.");
			            return "noMore";
			        }

			        // 최신 내역의 changeDate가 현재 시각 기준 ±10분 범위 내에 있는지 확인
			        LocalDateTime tenMinutesBefore = now.minusMinutes(10);
			        LocalDateTime tenMinutesAfter = now.plusMinutes(10);
			        log.info("Latest history changeDate: {}", latestHistory.getChangeDate());
			        log.info("10분 전 시각: {}", tenMinutesBefore);
			        log.info("10분 후 시각: {}", tenMinutesAfter);

			        if (latestHistory.getChangeDate().isAfter(tenMinutesBefore) && latestHistory.getChangeDate().isBefore(tenMinutesAfter)) {
			            log.info("이미 출석을 하였습니다. Latest history: {}", latestHistory);
			            return "already";
			        }

			        // 최신 내역의 changeDate가 범위 밖인 경우, PT 세션 내역을 차감 처리
			        PTSessionHistoryDTO ptSessionHistoryDTO = new PTSessionHistoryDTO();
			        ptSessionHistoryDTO.setUserId(matchedUserId);
			        ptSessionHistoryDTO.setChangeType("Deducted");
			        Long newChangeAmount = latestHistory.getChangeAmount() - 1;
			        ptSessionHistoryDTO.setChangeAmount(newChangeAmount);
			        ptSessionHistoryDTO.setReason("PT 출석으로 인한 감소");
			        // changeDate는 toEntity()에서 null이면 현재 시간으로 기본 설정됩니다.
			        PTSessionHistoryEntity ptSessionHistoryEntity = PTSessionHistoryEntity.toEntity(ptSessionHistoryDTO);
			        ptSessionHistoryRepository.save(ptSessionHistoryEntity);
			        log.info("PT 세션 차감 처리 완료. 새로운 changeAmount: {}", newChangeAmount);
			        log.info("historyList: {}", historyList);
			        return "success";
			    } else {
			        log.info("조건에 맞는 스케줄이 없습니다.");
			        return "false";
			    }
			}
}
