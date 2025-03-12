package fitmeup.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fitmeup.dto.HealthDataDTO;
import fitmeup.service.HealthDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
//@RequestMapping("/user")
@Slf4j
@Controller
public class MyPageController {

	private final HealthDataService healthDataService; // ✅ 서비스 주입

	/**
	 * 마이페이지에서 사용자 데이터 조회
	 */

	@GetMapping({ "/mypage" })
	public String index(
			Model model
			//,Long userId
			) {
		Long userId=1L;
		model.addAttribute("userId",userId);
		
		
		return "/mypage";
	}

	@GetMapping("/user/ptData")
	public String ptData() {
		return "/user/ptData";
	}
	
	
//	@Controller  
//	public class MyPageController {
//
//	    @GetMapping("/mypage")  // "/user/mypage"로 요청이 들어오면 실행됨
//	    public String mypage() {
//	        return "mypage"; // templates 폴더의 "mypage.html"을 찾아서 반환
//	    }
//	}

	
	

//	@GetMapping({"/userbodyData"})
//  public String userbodyData(
//		
//		 Model model) {
//		
//		 // ✅ HealthDataService를 통해 데이터 조회
//      List<HealthDataDTO> list = healthDataService.getAllHealthData(1L);
//		Long userId=1L;
//		model.addAttribute("userId",userId);
//	//List<HealthDataDTO> list = HealthDataEntity.selectAll(loginUser.getUserId());
//	
//	model.addAttribute("list", list);
//	return "/data/userbodyData";
//   
//     // return "/mypage";
//  }

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

	

	/*
	 * @PostMapping("/mypagesave") public ResponseEntity<String>
	 * insertHealthData(@RequestBody HealthDataDTO healthDTO) {
	 * HealthDataService.insert(healthDTO); return
	 * ResponseEntity.ok("Health data inserted successfully"); }
	 */

}
