package fitmeup.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
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
   
   
   public List<PTSessionHistoryDTO> getPTSessionHistory(Long userId) {
       List<PTSessionHistoryEntity> temp = ptSessionHistoryRepository.findByUserUserId(
           userId, Sort.by("changeDate").descending()
       );

       log.info("PT Session History List: {}", temp);

       // Entity → DTO 변환 후 리스트 반환
       return temp.stream()
                  .map(PTSessionHistoryDTO::fromEntity)
                  .collect(Collectors.toList());
   }
   
   public PTSessionHistoryDTO getPTSessionHistory2(Long userId) {
       List<PTSessionHistoryEntity> temp = ptSessionHistoryRepository.findByUserUserId(
           userId, Sort.by("changeDate").descending()
       );
       
        if(temp.isEmpty()) {
           PTSessionHistoryDTO dto = new PTSessionHistoryDTO();
           dto.setUserId(userId);
           dto.setChangeType(PTSessionHistoryEntity.ChangeType.Added.name());
           dto.setChangeAmount(0L);
           dto.setReason("새로운 PT계약 생성");
           ptSessionHistoryRepository.save(PTSessionHistoryEntity.toEntity(dto));
           temp = ptSessionHistoryRepository.findByUserUserId(userId, Sort.by("changeDate").descending());
         }

        PTSessionHistoryEntity latestHistory = temp.get(0);
       PTSessionHistoryDTO dto = PTSessionHistoryDTO.fromEntity(latestHistory);

         return dto;

   }
   
   public void savePT(PTSessionHistoryDTO ptSessionHistoryDTO) {
		
		PTSessionHistoryEntity entity = PTSessionHistoryEntity.toEntity(ptSessionHistoryDTO);
		log.info("=============entity123:{}" ,entity);
		ptSessionHistoryRepository.save(entity);
		
	}
	

}
