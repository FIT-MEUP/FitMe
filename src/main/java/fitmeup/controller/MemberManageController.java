package fitmeup.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fitmeup.dto.ChatUserDTO;
import fitmeup.dto.UserDTO;
import fitmeup.service.ChatUserService;
import fitmeup.service.UserService;
import java.util.List;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fitmeup.dto.ApproveRequestDTO;
import fitmeup.dto.LoginUserDetails;
import fitmeup.dto.PTSessionHistoryDTO;
import fitmeup.dto.TrainerApplicationDTO;
import fitmeup.entity.PTSessionHistoryEntity;
import fitmeup.entity.TrainerApplicationEntity;
import fitmeup.service.AnnouncementService;
import fitmeup.service.MealService;
import fitmeup.service.PTSessionHistoryService;
import fitmeup.service.ScheduleService;
import fitmeup.service.TrainerApplicationService;
import fitmeup.service.TrainerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MemberManageController {
    private final TrainerService trainerService;
    private final MealService mealService;
    private final TrainerApplicationService trainerApplicationService;
    private final UserService userService;
    private final ScheduleService scheduleService;
    private final AnnouncementService announcementService;
    private final PTSessionHistoryService ptSessionHistoryService;
    
    
    private final ChatUserService chatUserService;

    @GetMapping("trainer/memberManage")
    public String memberManagePage(
                                    @AuthenticationPrincipal LoginUserDetails loginUser,
                                    Model model) {
        // findbyId로 대충 특정했다고 가정하고
        if("Trainer".equals(loginUser.getRoles())){
            Long trainerNum = loginUser.getUserId();
            List<TrainerApplicationDTO> ApprovedList = trainerApplicationService.getApplicationById(trainerNum, TrainerApplicationEntity.Status.Approved);
            List<TrainerApplicationDTO> PendingList = trainerApplicationService.getApplicationById(trainerNum, TrainerApplicationEntity.Status.Pending);
//            for(String role : roleNames) {
//            	log.info("====================={}",role);
//            }
            log.info("=====================ApprovedList:{}", ApprovedList);
            log.info("=====================PendingList:{}", PendingList);

           
            // html에 foreach 돌려서 하면 될듯?
            model.addAttribute("ApprovedList", ApprovedList);
            model.addAttribute("PendingList", PendingList);
            model.addAttribute("AnnouncementContent",  announcementService.sendAnnouncement(trainerNum));

            // ChatUserService에서 채팅 대상 유저 정보 조회 (ChatUserDTO: userId, unreadCount, online)
            List<ChatUserDTO> chatUserList = chatUserService.getChatUserList();

            // userId를 키로 하는 Map 생성 (채팅 대상의 unreadCount 등 빠르게 조회하기 위함)
            Map<Long, ChatUserDTO> chatUserMap = chatUserList.stream()
                .collect(Collectors.toMap(ChatUserDTO::getUserId, Function.identity(), (oldValue, newValue) -> oldValue));

            model.addAttribute("ChatUserMap", chatUserMap);

            // JSON 문자열 변환 처리 (예외를 catch하여 처리)
            String chatUserMapJson = "";
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                chatUserMapJson = objectMapper.writeValueAsString(chatUserMap);
            } catch (JsonProcessingException e) {
                // 필요에 따라 로깅 후, 기본값 사용
                e.printStackTrace();
            }
            model.addAttribute("chatUserMapJson", chatUserMapJson);

            return "manage/memberManage"; // Thymeleaf 템플릿 뷰 이름 (확장자 제외)
        } else {
            return "redirect:/";
        }
    }

    // 신청 승인 API
    @PostMapping("/trainer/approve")
    @ResponseBody
    public ResponseEntity<String> approveApplication(@RequestBody ApproveRequestDTO approveRequestDTO) {
    	log.info("===============수락:{}",approveRequestDTO.getApplicationId());
        trainerApplicationService.updateApplicationStatus(approveRequestDTO.getApplicationId(), TrainerApplicationEntity.Status.Approved);
        return ResponseEntity.ok("Application approved successfully.");
    }

    // 신청 거절 API
    @PostMapping("/trainer/reject")
    @ResponseBody
    public ResponseEntity<String> rejectApplication(@RequestBody ApproveRequestDTO approveRequestDTO) {
        trainerApplicationService.updateApplicationStatus(approveRequestDTO.getApplicationId(), TrainerApplicationEntity.Status.Rejected);
        return ResponseEntity.ok("Application rejected successfully.");
    }

    // 회원 PT 선택 API
    @GetMapping("/trainer/selectPT")
    @ResponseBody
    public PTSessionHistoryDTO selectApplication(@RequestParam(name="applicationId") Long applicationId, Model model) {
        PTSessionHistoryDTO dto = new PTSessionHistoryDTO();
        
        dto.setUserId(applicationId);
        dto.setChangeType(PTSessionHistoryEntity.ChangeType.Added.name());
        dto.setChangeAmount(0L);
        dto.setReason("새로운 PT계약 생성");

        
        return PTSessionHistoryDTO.fromEntity(scheduleService.selectfirstByUserDTO(dto)); 
    }
    
    // 회원 PT 업데이트 API
    @PostMapping("/trainer/updatePT")
    @ResponseBody
    public boolean updateApplication(@RequestBody  PTSessionHistoryDTO ptSessionHistoryDTO) {
    	PTSessionHistoryDTO PTdto = new PTSessionHistoryDTO();
    	PTdto.setUserId(ptSessionHistoryDTO.getUserId());
    	PTdto.setChangeType(PTSessionHistoryEntity.ChangeType.Added.name());
    	PTdto.setChangeAmount(ptSessionHistoryDTO.getChangeAmount());
        PTdto.setReason(ptSessionHistoryDTO.getReason());
        
        
        ptSessionHistoryService.savePT(PTdto);
        
        return true; 
    }
    
    // 트레이너 공지사항 API
    @PostMapping("/trainer/saveAnnouncement")
    public boolean saveTrainerAnnouncement(@RequestBody String announcement,@AuthenticationPrincipal LoginUserDetails loginUser) {
    	log.info("=====================anoun{}",announcement);
        announcementService.saveAnnouncement(announcement, loginUser.getUserId());
        
        return true;
        
    }
    
    
    

//    // 로그인한 사용자의 정보를 데이터베이스에서 조회하여 UserDTO로 반환하는 엔드포인트
//    @PostMapping("/trainer/selectLoginUser")
//    @ResponseBody
//    public UserDTO selectLoginUser(@AuthenticationPrincipal LoginUserDetails loginUser) {
//        // loginUser.getUserId()를 기반으로 DB에서 사용자 정보를 조회하여 UserDTO 반환
//        return userService.getUserById(loginUser.getUserId());
//    }
//
//    // 대상 회원의 정보를 반환하는 엔드포인트 (ApprovedItem.applicationId 기반)
//    @PostMapping("/trainer/selectUser")
//    @ResponseBody
//    public UserDTO selectUser(@org.springframework.web.bind.annotation.RequestParam("applicationId") Long applicationId) {
//        return trainerApplicationService.getUserByApplicationId(applicationId);
//    }
}
