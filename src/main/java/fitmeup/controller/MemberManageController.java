package fitmeup.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fitmeup.dto.ChatUserDTO;
import fitmeup.dto.UserDTO;
import fitmeup.service.ChatUserService;
import fitmeup.service.UserService;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fitmeup.dto.ApproveRequestDTO;
import fitmeup.dto.LoginUserDetails;
import fitmeup.dto.TrainerApplicationDTO;
import fitmeup.entity.TrainerApplicationEntity;
import fitmeup.service.MealService;
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
    private final ChatUserService chatUserService;

    @GetMapping("/trainer/memberManage")
    public String memberManagePage(@AuthenticationPrincipal LoginUserDetails loginUser, Model model) {
        // loginUser.getRoles()가 문자열이라면 equals()로 비교합니다.
        if ("Trainer".equals(loginUser.getRoles())) {
            Long trainerNum = loginUser.getUserId();
            List<TrainerApplicationDTO> ApprovedList = trainerApplicationService.getApplicationById(trainerNum, TrainerApplicationEntity.Status.Approved);
            List<TrainerApplicationDTO> PendingList = trainerApplicationService.getApplicationById(trainerNum, TrainerApplicationEntity.Status.Pending);
            model.addAttribute("ApprovedList", ApprovedList);
            model.addAttribute("PendingList", PendingList);

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

    // 회원 선택 API
    @GetMapping("/trainer/select")
    @ResponseBody
    public String selectApplication(@RequestParam(name="applicationId") Long applicationId) {
    	String name = trainerApplicationService.selectOne(applicationId);

        return name;
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
