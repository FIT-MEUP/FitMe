package fitmeup.controller;

import java.time.LocalDateTime;
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
import fitmeup.entity.PtSessionHistoryEntity;
import fitmeup.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

private final ScheduleService scheduleService;
	
	
	//ScheuldeDTO를 list형태로 front단에 보내주는 method
	@GetMapping({"/firstUserCalendar"})
	public String index(Model model
//			 ,@RequestParam(name = "userId", defaultValue = "5") Long userId
			 ,@AuthenticationPrincipal LoginUserDetails loginUser
			) {
		Long userId= loginUser.getUserId();
//		 Long trainerId=scheduleService.findTrainerId(4L);
		//정상 작동 5->2 4->1
		  Long apptrainerId=scheduleService.findTrainerId(userId);
		
		  //UserEntity의 UserId를 넣어야함 즉 trainerId가 2인 유저가 UserId가 3이어야 하니깐 그걸 넣어야함
		  //즉 trainerId를 통해 UserId를 찾는 작업을 해야함
		  Long trainerId= scheduleService.findTrainerUserId(apptrainerId);
	    List<TrainerScheduleDTO> list = scheduleService.selectTrainerScheduleAll(trainerId);
	    
	    model.addAttribute("list", list);
	
//	    model.addAttribute("userId",4);
	    model.addAttribute("userId",userId);
	    
	    
	    List<ScheduleDTO> userlist = scheduleService.selectAll(apptrainerId);
	
	    
	    
	   
	    model.addAttribute("trainerId",trainerId);
	    model.addAttribute("userlist",userlist);
	   
	    log.info(trainerId.toString());
	    log.info(userlist.toString());
	    
	 // 현재 로그인한 사용자의 userName을 추가합니다.
        String userName = scheduleService.findUserName(userId);
        model.addAttribute("userName", userName);
	    
      //userId를 통해 ptSessionHistory에서 가장 최근거를 가져오는 메소드
        PtSessionHistoryEntity temp = scheduleService.selectfirstByUserId(userId);
        if(temp!=null) {
        Long changeAmount=temp.getChangeAmount();
        		model.addAttribute("changeAmount",changeAmount);
        		log.info("changeAmount{}",changeAmount);}
        else {
        	   Long changeAmount=0L;
       		model.addAttribute("changeAmount",changeAmount);
        }
        
        
		return "userschedule";
	
	
	}
	

	
	
	
	@GetMapping("/calendar")
	@ResponseBody
	public String trainercalendarInsert(
	        @RequestParam("start") String start,
	        @RequestParam("end") String end,
	        @RequestParam("userId") Long userId) {

		log.info("userId = {}",userId);
	    // 요청 받은 start, end를 LocalDateTime으로 파싱
	    LocalDateTime newStart = LocalDateTime.parse(start, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	    LocalDateTime newEnd = LocalDateTime.parse(end, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

	    // userId를 통해 trainerId 찾기
//	    Long trainerId = scheduleService.findTrainerId(userId);

	    Long apptrainerId=scheduleService.findTrainerId(userId);
		log.info("apptrainerId: {}",apptrainerId);//2
//		
//		  //UserEntity의 UserId를 넣어야함 즉 trainerId가 2인 유저가 UserId가 3이어야 하니깐 그걸 넣어야함
//		  //즉 trainerId를 통해 UserId를 찾는 작업을 해야함
		  Long trainerId= scheduleService.findTrainerUserId(apptrainerId);
		  log.info("trainerId: {}",trainerId);//3
	    
		  
	    // 1. 가능한 시간(TrainerScheduleDTO 목록) 가져오기 UserEntity 에 User를 가져와야함
	    List<TrainerScheduleDTO> availableIntervals = scheduleService.selectTrainerScheduleAll(trainerId);
	    log.info(availableIntervals.toString());
	    // 요청 시간(newStart~newEnd)이 가능한 시간 중 하나의 범위 안에 있는지 확인
	    boolean withinRange = availableIntervals.stream()
	            .anyMatch(interval ->
	                    !newStart.isBefore(interval.getStartTime()) && !newEnd.isAfter(interval.getEndTime())
	            );
	    if (!withinRange) {
	        return "noRange";
	    }

	    // 2. 기존 예약(ScheduleDTO 목록) 가져오기
	    //trainer->trainerId를 가져와야함
	    List<ScheduleDTO> existingSchedules = scheduleService.selectAll(apptrainerId);
	    log.info(existingSchedules.toString());
	    // 요청 시간과 기존 예약이 겹치는지 확인 (겹치면 overlap 발생)
	    boolean overlaps = existingSchedules.stream()
	            .anyMatch(schedule ->
	                    newStart.isBefore(schedule.getEndTime()) && newEnd.isAfter(schedule.getStartTime())
	            );
	    if (overlaps) {
	        return "alreadySchedule";
	    }
	    
	 // 3. 추가 제약: 현재 로그인한 사용자(userId)가 이미 미래 예약을 가지고 있는지 확인
	    List<ScheduleDTO> mySchedules = scheduleService.selectAllByUserId(userId);
	    boolean alreadyHaveFutureSchedule = mySchedules.stream()
	            .anyMatch(schedule -> schedule.getStartTime().isAfter(LocalDateTime.now()));
	    if (alreadyHaveFutureSchedule) {
	        return "alreadyHaveSchedule";
	    }

	    // 두 조건 모두 만족하면 예약 생성
	    ScheduleDTO scheduleDTO = new ScheduleDTO();
	    scheduleDTO.setUserId(userId);
	    scheduleDTO.setTrainerId(apptrainerId);
	    scheduleDTO.setStartTime(newStart);
	    scheduleDTO.setEndTime(newEnd);
	    scheduleService.insertSchedule(scheduleDTO);

	    log.info("Start Time: " + newStart);
	    log.info("End Time: " + newEnd);

	    return "success";
	}
	
	
	
	@GetMapping("/usercalendar/delete")
	@ResponseBody
	public boolean boardDelete(
	    @RequestParam(name="scheduleId") Integer scheduleId
	) {
	    log.info("trainerSchedule삭제    {}",scheduleId.toString());
	    scheduleService.deleteSchedule(scheduleId);
	    return true;
	}
	
	
	

}
