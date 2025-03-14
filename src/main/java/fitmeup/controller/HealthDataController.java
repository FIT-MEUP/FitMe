package fitmeup.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fitmeup.dto.HealthDataDTO;
import fitmeup.service.HealthDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class HealthDataController {
	private final HealthDataService healthDataService;
	/**
	 * 신체 데이터 수정 화면 요청
	 * 
	 * @return
	 */
	
	  @GetMapping("/userbodyData") 
	  public String userbodyData(@RequestParam(name="userId") Long userId ,Model model ) {
	  List <HealthDataDTO> list=healthDataService.listFindByUserId(userId);
	 
	  
	  model.addAttribute("list",list); // model.addAttribute("userId",userId);
	
	 
	  return "/user/userbodyData"; }
	 

	/**
	 * 신체 데이터 등록 처리 요청
	 * 
	 * @param bookDTO
	 * @return ajax로 결과 반환
	 */
	@PostMapping("/userbodyData")
	@ResponseBody
	public String saveHealthData(@ModelAttribute HealthDataDTO healthDTO) {
		healthDataService.insert(healthDTO);
		return "OK";
	}

	/**
	 * 저장된 신체 데이터 신체 내역 페이지에서 삭제
	 * 
	 * @return
	 */

	@PostMapping("/deleteHealthData")
	@ResponseBody
	public String deleteHealthData(@RequestParam("dataId") Long dataId) {

		healthDataService.delete(dataId);
		return "OK";
	}

	/**
	 * 저장된 신체 데이터 신체 내역 페이지에서 수정
	 * 
	 * @return
	 */
	@PostMapping("/updateHealthData")
	@ResponseBody
	public String updateHealthData(@ModelAttribute HealthDataDTO healthDTO) {
		try {
			healthDataService.update(healthDTO);
			return "OK"; // 성공하면 "OK" 반환
		} catch (Exception e) {
			return "FAIL"; // 실패하면 "FAIL" 반환
		}
	}
	

	/**
	 * 가장 최신데이터 반환 api
	 * 
	 * @return
	 */
	
//	@GetMapping("/latestHealthData")
//	@ResponseBody
//	public HealthDataDTO getLatestHealthData(@RequestParam(name="userId") Long userId) {
//	    return healthDataService.getLatestHealthData(userId);
//	}
	
	@GetMapping("/latestHealthData")
	public ResponseEntity<HealthDataDTO> getLatestHealthData(
	        Long userId) {
	    
	    HealthDataDTO latestData = healthDataService.getLatestHealthData(userId);
	    
	    if (latestData != null) {
	        return ResponseEntity.ok(latestData);
	    } else {
	        return ResponseEntity.notFound().build(); // 404 반환 (데이터 없을 경우)
	    }
	}

	
	@GetMapping("/healthDataHistory")
	@ResponseBody
	public List<HealthDataDTO> getHealthDataHistory(@RequestParam(name="userId") Long userId) {
	    return healthDataService.listFindByUserId(userId); // 해당 사용자의 모든 데이터 가져오기
	}

}
