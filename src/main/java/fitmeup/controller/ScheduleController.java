package fitmeup.controller;


import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fitmeup.dto.TrainerScheduleDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Controller
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

	private final ScheduleService scheduleService;
	
	@GetMapping({"/",""})
	public String index(Model model) {
		System.out.println("dddddd");
	    List<TrainerScheduleDTO> list = scheduleService.selectTrainerScheduleAll();
	    model.addAttribute("list", list);
	    
	   log.info(list.toString());
		return "trainerschedule";
	}
	
	
	
	
	@GetMapping("/trainercalendar")
	@ResponseBody
	public String trainercalendarInsert(
	        @RequestParam("start") String start,
	        @RequestParam("end") String end
	) {
	    // 오프셋이 포함된 문자열을 파싱한 후 바로 LocalDateTime으로 변환 (추가 변환 X)
	    LocalDateTime startTime = OffsetDateTime
	                                .parse(start, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
	                                .toLocalDateTime();
	    LocalDateTime endTime = OffsetDateTime
	                                .parse(end, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
	                                .toLocalDateTime();

	    TrainerScheduleDTO trainerScheduleDTO = new TrainerScheduleDTO();
	    trainerScheduleDTO.setStartTime(startTime);
	    trainerScheduleDTO.setEndTime(endTime);

	    scheduleService.insertTrainerSchedule(trainerScheduleDTO);
	    log.info("Start Time: " + startTime);
	    log.info("End Time: " + endTime);
	    
	    return "success";
	}
	
	@GetMapping("/trainercalendar/delete")
	@ResponseBody
	public boolean boardDelete(
			@RequestParam(name="id") Integer trainerScheduleId
			) {
		log.info(trainerScheduleId.toString());
		scheduleService.deleteOne(trainerScheduleId);
		
		
		
		return true;
	}
	
	
}
