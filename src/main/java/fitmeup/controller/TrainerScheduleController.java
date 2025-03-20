package fitmeup.controller;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fitmeup.dto.LoginUserDetails;
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
		@GetMapping({"/firstTrainerSchedule"})
		public String index(Model model
				,@AuthenticationPrincipal LoginUserDetails loginUser
				) {
			Long userId= loginUser.getUserId();
			Long trainerId = scheduleService.findTrainertrainerId(userId);
			log.info("userId ===== {}",userId);
			
//			List<TrainerScheduleDTO> list = scheduleService.selectTrainerScheduleAll(1L);
				List<TrainerScheduleDTO> list = scheduleService.selectTrainerScheduleAll(userId);
		    model.addAttribute("list", list);
		    log.info(list.toString());
//		    model.addAttribute("trainerId",1);
		    model.addAttribute("trainerId",trainerId);
		    log.info("trainerId4321{}",trainerId);
		  

//		    List<ScheduleDTO> userlist = scheduleService.selectAll(1L);
		    
		    //이건 trainerEntity의 trainerId를 넣어야함를 넣어야함
		    
		    log.info("트레이너 입장 trainerId = {}",trainerId);
		     List<ScheduleDTO> userlist = scheduleService.selectAll(trainerId);
		     log.info("userlist.size()userlist.size()       {}",userlist.size());
		    
		   
		    
		    model.addAttribute("userlist",userlist);
		   log.info(list.toString());
		   log.info("             {}",userlist.toString());
		   
		   System.out.println("Default TimeZone: " + java.util.TimeZone.getDefault().getID());
			
		   
		   
		   //User_id를 통해서 trainerEntity의 Trainerid가져오기
		   Long realTrainerId= scheduleService.findTrainerIdbyUserId(userId);
		   log.info("realTrainerId: {}",realTrainerId);
		   model.addAttribute("realTrainerId",realTrainerId);
		   
		   
		   
		   return "schedule/trainerschedule";
		}
	
	
	
	//TrainerSchedule을 Create하는 method
	@GetMapping("/trainercalendar")
	@ResponseBody
	public String trainercalendarInsert(
	        @RequestParam("start") String start,
	        @RequestParam("end") String end
	        ,@AuthenticationPrincipal LoginUserDetails loginUser
	) {
		Long trainerId= loginUser.getUserId();
	    // 오프셋이 포함된 문자열을 파싱한 후 바로 LocalDateTime으로 변환 (추가 변환 X)
	    LocalDateTime startTime = OffsetDateTime
	                                .parse(start, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
	                                .toLocalDateTime();
	    LocalDateTime endTime = OffsetDateTime
	                                .parse(end, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
	                                .toLocalDateTime();

	    log.info("end : {}", endTime);
	    log.info("start : {}", startTime);
	    Long realtrainerId = scheduleService.findTrainerIdbyUserId(trainerId);
	    TrainerScheduleDTO trainerScheduleDTO = new TrainerScheduleDTO();
	    trainerScheduleDTO.setStartTime(startTime);
	    trainerScheduleDTO.setEndTime(endTime);
	    trainerScheduleDTO.setTrainerId(realtrainerId);

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
	
	
	@ResponseBody
	@GetMapping("/ptSessionHistoryChangeAmount")
	public String ptSessionHisotryChangeAmount(
			//@RequestParam(name="trainerId") Long userId
			@AuthenticationPrincipal LoginUserDetails loginUser
			){
		Long userId= loginUser.getUserId();
		log.info("userId============== {}",userId);
		String temp=scheduleService.minusChangeAmount(userId);
		log.info(temp);
		
		return temp;
	}
	
}