package fitmeup.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fitmeup.dto.TrainerScheduleDTO;
import fitmeup.entity.TrainerScheduleEntity;
import fitmeup.repository.TrainerScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {
	private final ScheduleRepository scheduleRepository;
	private final TrainerScheduleRepository trainerScheduleRepository;
	
	//trainerSchedule Read
	public List<TrainerScheduleDTO> selectTrainerScheduleAll(){
		List<TrainerScheduleEntity> temp = trainerScheduleRepository.findAll();

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
	public void deleteOne(Integer trainerScheduleId) {
		Optional<TrainerScheduleEntity> temp = trainerScheduleRepository.findById(trainerScheduleId);
		
		if(!temp.isPresent()) return ;
		
		trainerScheduleRepository.deleteById(trainerScheduleId);
	}
	
	
	
	public List<ScheduleDTO> selectAll(){
	List<ScheduleEntity> temp = scheduleRepository.findAll(Sort.by(Sort.Direction.DESC, "startTime"));

	List<ScheduleDTO> list = new ArrayList<>();

	temp.forEach((entity) -> list.add(ScheduleDTO.toDTO(entity)));

	return list;
}
	public void insertSchedule(ScheduleDTO trainerScheduleId) {
		
		
		ScheduleEntity scheduleEntity = ScheduleEntity.toEntity(trainerScheduleId);
		
		scheduleRepository.save(scheduleEntity);
	}

	

}