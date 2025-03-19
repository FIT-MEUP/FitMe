package fitmeup.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fitmeup.dto.ChatUserDTO;
import fitmeup.dto.HealthDataDTO;
import fitmeup.dto.WorkDTO;
import fitmeup.service.ChatUserService;
import fitmeup.service.HealthDataService;
import fitmeup.service.UserService;
import java.util.HashMap;
import fitmeup.service.WorkService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fitmeup.dto.ApproveRequestDTO;
import fitmeup.dto.LoginUserDetails;
import fitmeup.dto.MealDTO;
import fitmeup.dto.PTSessionHistoryDTO;
import fitmeup.dto.TrainerApplicationDTO;
import fitmeup.dto.UserDTO;
import fitmeup.entity.PTSessionHistoryEntity;
import fitmeup.entity.TrainerApplicationEntity;
import fitmeup.entity.UserEntity;
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
    private final MealService mealService;
    private final TrainerApplicationService trainerApplicationService;
    private final ScheduleService scheduleService;
    private final AnnouncementService announcementService;
    private final PTSessionHistoryService ptSessionHistoryService;
    private final WorkService workService;
	private final HealthDataService healthDataService; // ✅ 서비스 주입

    
    private final ChatUserService chatUserService;
    private final UserService userService;

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

            // (2) ApprovedList의 각 applicationId별 userId를 통해 user.isOnline을 조회
            //     userOnlineMap: key=applicationId, value=(true/false)
            Map<Long, Boolean> userOnlineMap = new HashMap<>();
            for (TrainerApplicationDTO item : ApprovedList) {
                Long userId = item.getUserId();  // 신청서상 회원 ID
                Long applicationId = item.getApplicationId(); // 신청서 ID


                UserDTO userDTO = userService.getUserById(userId);

                boolean online = Boolean.TRUE.equals(userDTO.getIsOnline());

                userOnlineMap.put(applicationId, online);
            }


            // userOnlineMap을 Thymeleaf로 넘겨서, ApprovedItem.applicationId → userOnlineMap → online
            model.addAttribute("userOnlineMap", userOnlineMap);


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
    public PTSessionHistoryDTO selectApplication(@RequestParam(name="userId") Long userId, Model model) {

        
        return PTSessionHistoryDTO.fromEntity(scheduleService.selectfirstByUserId(userId)); 
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
    
//    // 트레이너 공지사항 API
//    @PostMapping("/trainer/saveAnnouncement")
//    public boolean saveTrainerAnnouncement(@RequestBody String announcement,@AuthenticationPrincipal LoginUserDetails loginUser) {
//        announcementService.saveAnnouncement(announcement, loginUser.getUserId());
//        
//        return true;
//        
//    }
        @PostMapping("/trainer/saveAnnouncement")
        @ResponseBody
        public ResponseEntity<Boolean> saveTrainerAnnouncement(
            @RequestBody Map<String, String> requestData, 
            @AuthenticationPrincipal LoginUserDetails loginUser) {

            String announcement = requestData.get("announcement");
            announcementService.saveAnnouncement(announcement, loginUser.getUserId());

            return ResponseEntity.ok(true); // ResponseEntity로 감싸서 반환
        }
    


    // 오늘의 식단 미리보기 API
    @GetMapping("/trainer/mealPreview")
    @ResponseBody
    public List<MealDTO> mealPreview(@RequestParam(name="userId") Long userId) {
        List<MealDTO> meals = Collections.emptyList(); // ✅ 기본값: 빈 리스트
        String currentDate = LocalDate.now().toString();

        Long loggedInUserId = userId;

        meals = mealService.getMealsByUserAndDate(userId, LocalDate.parse(currentDate), loggedInUserId, UserEntity.Role.Trainer.name());

        if (meals.isEmpty()) {
            return null; // 또는 적절한 예외 처리
        }
        
        return meals;
        
    }

    // 오늘의 운동 미리보기 API
    @GetMapping("/trainer/workPreview")
    @ResponseBody
    public List<WorkDTO> workPreview(@RequestParam(name="userId") Long userId) {
        String currentDate = LocalDate.now().toString();
        Long loggedInUserId = userId;

        // 운동 기록 조회
        List<WorkDTO> workouts = workService.getUserWorkoutsByDate(userId, LocalDate.parse(currentDate), loggedInUserId,UserEntity.Role.Trainer.name());
        
        if (workouts.isEmpty()) {
        	
            return null; // 또는 적절한 예외 처리
        }
        
        return workouts;
        
    }

    // 회원정보 미리보기 API
    @GetMapping("/trainer/userPreview")
    @ResponseBody
    public HealthDataDTO userPreview(@RequestParam(name="userId") Long userId) {
        Long loggedInUserId = userId;

        HealthDataDTO latestData = healthDataService.getLatestHealthData(loggedInUserId);
    	log.info("===============latestData:{}", latestData);
        
	    if (latestData == null) {
	    	return null;
	    	
	    }
        
        return latestData;
        
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
