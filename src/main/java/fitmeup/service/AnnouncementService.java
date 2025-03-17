package fitmeup.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fitmeup.entity.AnnouncementEntity;
import fitmeup.entity.PTSessionHistoryEntity;
import fitmeup.entity.UserEntity;
import fitmeup.repository.AnnouncementRepository;
import fitmeup.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnnouncementService {
	private final AnnouncementRepository announcementRepository;
	private final UserRepository userRepository;
	
	public boolean saveAnnouncement(String announcement, Long userId) {
		AnnouncementEntity entity = new AnnouncementEntity();
		Optional<UserEntity> user = userRepository.findById(userId);
		
		entity.setContent(announcement);
		entity.setUser(user.get());
		
		announcementRepository.save(entity);
		
		return true;
		
	}
	
	public String sendAnnouncement(Long userId) {
		 List<AnnouncementEntity> list =announcementRepository.findByUserUserId(userId, Sort.by(Sort.Direction.DESC, "createdAt"));
		 if(list.isEmpty()) {
			 return "트레이너 공지 사항 내용";

		 }
		 return list.get(0).getContent();
	}
	

}
