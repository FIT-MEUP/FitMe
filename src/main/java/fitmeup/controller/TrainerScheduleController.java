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

import fitmeup.dto.ScheduleDTO;
import fitmeup.dto.TrainerScheduleDTO;
import fitmeup.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class TrainerScheduleController {

	
	private final ScheduleService scheduleService;
	
	
	//TrainerScheuldeDTO를 list형태로 front단에 보내주는 method
//	@GetMapping({"/",""})
	public String index(Model model
//			,Long trainerId
			) {
		System.out.println("dddddd");

		List<TrainerScheduleDTO> list = scheduleService.selectTrainerScheduleAll(1L);
		//	List<TrainerScheduleDTO> list = scheduleService.selectTrainerScheduleAll(trainerId);
	    model.addAttribute("list", list);
	    log.info(list.toString());
	    model.addAttribute("trainerId",1);
//	    model.addAttribute("trainerId",trainerId);
	  

	    List<ScheduleDTO> userlist = scheduleService.selectAll(1L);
	    // List<ScheduleDTO> userlist = scheduleService.selectAll(trainerId);
	    
	   
	    
	    model.addAttribute("userlist",userlist);
	   log.info(list.toString());
	   log.info("             {}",userlist.toString());
		return "schedule/trainerschedule";
	}
	
	
	
	//TrainerSchedule을 Create하는 method
	@GetMapping("/trainercalendar")
	@ResponseBody
	public String trainercalendarInsert(
	        @RequestParam("start") String start,
	        @RequestParam("end") String end
	        ,@RequestParam("trainerId") Long trainerId
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
	    trainerScheduleDTO.setTrainerId(trainerId);

	    scheduleService.insertTrainerSchedule(trainerScheduleDTO);
	    log.info("Start Time: " + startTime);
	    log.info("End Time: " + endTime);
	    
	    return "success";
	}
	
	//trainerSchedule을 Delete하는 메소드
	@GetMapping("/trainercalendar/delete")
	@ResponseBody
	public boolean boardDelete(
			@RequestParam(name="id") Integer trainerScheduleId
			) {
		log.info(trainerScheduleId.toString());
		scheduleService.deleteTrainerSchedule(trainerScheduleId);
		
		
		
		return true;
	}
	
}
