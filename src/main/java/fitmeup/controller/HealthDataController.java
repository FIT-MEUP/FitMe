package fitmeup.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
	 * @return
	 */
	@GetMapping("/userbodyData")
	public String userbodyData(
			@RequestParam(name="userId") Long userId
			,Model model
			) {

	List <HealthDataDTO> list=healthDataService.listFindByUserId(userId);
		
		model.addAttribute("list",list);
//		model.addAttribute("userId",userId);
		log.info(list.toString());
		log.info("Retrieved list size: {}", list.size());
		return "/user/userbodyData";
	}

	/**
	 * 신체 데이터 등록 처리 요청 
	 * @param bookDTO
	 * @return ajax로 결과 반환
	 */
	@PostMapping("/userbodyData")
	@ResponseBody
	public String saveHealthData(@ModelAttribute HealthDataDTO healthDTO) {
		healthDataService.insert(healthDTO);
		return "OK";
	}
//foreach , 수정삭제 누를 시 ajax로 할거면, (data_id넘겨야됨)..


}
