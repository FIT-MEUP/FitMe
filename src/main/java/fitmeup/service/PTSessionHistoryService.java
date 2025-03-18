package fitmeup.service;

import org.springframework.stereotype.Service;

import fitmeup.dto.PTSessionHistoryDTO;
import fitmeup.entity.PTSessionHistoryEntity;
import fitmeup.repository.PTSessionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PTSessionHistoryService {
	private final PTSessionHistoryRepository ptSessionHistoryRepository;

	public void savePT(PTSessionHistoryDTO ptSessionHistoryDTO) {
		
		PTSessionHistoryEntity entity = PTSessionHistoryEntity.toEntity(ptSessionHistoryDTO);
		log.info("=============entity123:{}" ,entity);
		ptSessionHistoryRepository.save(entity);
		
	}
	
	
	
	
}
