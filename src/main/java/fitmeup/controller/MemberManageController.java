package fitmeup.controller;

import fitmeup.dto.UserDTO;
import fitmeup.service.UserService;
import java.util.List;

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

    @GetMapping("/trainer/memberManage")
    public String memberManagePage(@AuthenticationPrincipal LoginUserDetails loginUser, Model model) {
        // loginUser.getRoles()가 문자열이라면 equals()로 비교합니다.
        if ("Trainer".equals(loginUser.getRoles())) {
            Long trainerNum = loginUser.getUserId();
            List<TrainerApplicationDTO> ApprovedList = trainerApplicationService.getApplicationById(trainerNum, TrainerApplicationEntity.Status.Approved);
            List<TrainerApplicationDTO> PendingList = trainerApplicationService.getApplicationById(trainerNum, TrainerApplicationEntity.Status.Pending);
            model.addAttribute("ApprovedList", ApprovedList);
            model.addAttribute("PendingList", PendingList);
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
