package fitmeup.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fitmeup.dto.HealthDataDTO;
import fitmeup.dto.LoginUserDetails;
import fitmeup.dto.PTSessionHistoryDTO;
import fitmeup.entity.UserEntity;
import fitmeup.service.HealthDataService;
import fitmeup.service.PTSessionHistoryService;
import fitmeup.service.TrainerApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
//@RequestMapping("/user")
@Slf4j
@Controller
public class MyPageController {

	private final HealthDataService healthDataService; // ✅ 서비스 주입
	private final PTSessionHistoryService ptSessionHistoryService;
	private final TrainerApplicationService trainerApplicationService;

	/**
	 * 마이페이지에서 사용자 데이터 조회
	 */

	@GetMapping({"/mypage"})
	public String index(
			Model model	
			,@AuthenticationPrincipal  LoginUserDetails loginUser
			 , @RequestParam(name="userId", defaultValue="0") Long userIdRequest
			) 
	{
		Long userId=0L;
		if(userIdRequest==0) {
			userId=loginUser.getUserId();
		}else {
		 userId=userIdRequest;	
		}
		log.info("userId ======{}",userId);	
			model.addAttribute("userId",userId);
			
			//  현재 로그인한 사용자 role
		    String role = loginUser.getRoles();
		    model.addAttribute("role", role);

		    //  트레이너가 로그인한 경우, 승인된 회원 목록 조회 후 전달
		    if ("Trainer".equals(role)) {
		        List<UserEntity> trainerMembers = trainerApplicationService.getTrainerMembers(loginUser.getUserId());
		        model.addAttribute("trainerMembers", trainerMembers);
		    }
		
	    // 최신 데이터 가져오기
	    HealthDataDTO latestData = healthDataService.getLatestHealthData(userId);
	    if (latestData == null) {
	        // 데이터가 없으면 기본값 세팅
	        latestData = new HealthDataDTO();
	        latestData.setHeight(BigDecimal.ZERO);
	        latestData.setWeight(BigDecimal.ZERO);
	        latestData.setMuscleMass(BigDecimal.ZERO);
	        latestData.setFatMass(BigDecimal.ZERO);
	        latestData.setBmi(BigDecimal.ZERO);
	        latestData.setBasalMetabolicRate(BigDecimal.ZERO);
	    }
	    
	    model.addAttribute("latestData", latestData);
		
		
		return "/mypage";
	}
	


	/**
	 * 사용자 건강 데이터 저장 (JSON 요청)
	 */
	@ResponseBody
	@PostMapping("/application/json")
	public ResponseEntity<String> insertHealthData(@RequestBody HealthDataDTO healthDTO
//			,@RequestParam(name="date") String date
			) {
		log.info(healthDTO.toString());
//		log.info(date);
		healthDataService.insert(healthDTO);
		return ResponseEntity.ok("Health data inserted successfully");
	}
	
	@GetMapping("/user/ptData")
    public String userPtData(
        Model model,
        LoginUserDetails loginUser,
        @RequestParam(name="userId", defaultValue="0") Long userIdRequest
    ) {
        Long userId = (userIdRequest == 0) ? loginUser.getUserId() : userIdRequest;

        // ✅ 인스턴스 메서드로 호출
        List<PTSessionHistoryDTO> list = ptSessionHistoryService.getPTSessionHistory(userId);

        model.addAttribute("list", list);

        return "/user/ptData";
    }
}