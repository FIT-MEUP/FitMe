package fitmeup.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fitmeup.dto.LoginUserDetails;
import fitmeup.dto.TrainerDTO;
import fitmeup.entity.TrainerApplicationEntity;
import fitmeup.entity.TrainerEntity;
import fitmeup.entity.TrainerPhotoEntity;
import fitmeup.entity.UserEntity;
import fitmeup.repository.TrainerApplicationRepository;
import fitmeup.service.TrainerApplicationService;
import fitmeup.service.TrainerService;
import fitmeup.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TrainerController { 
	private final UserService userService;
    private final TrainerService trainerService;
    private final TrainerApplicationService trainerApplicationService;
    private final TrainerApplicationService consultationService;
    private final TrainerApplicationRepository trainerApplicationRepository;

    @GetMapping({"/", "", "/trainers"})
    public String trainers(Model model) {
        List<TrainerEntity> trainers = trainerService.getAllTrainers();
        model.addAttribute("trainers", trainers);
        return "trainers"; 
    }

    @GetMapping("/trainer/{trainerId}")
    public String trainerDetail(@PathVariable("trainerId") Long trainerId, Model model,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        TrainerEntity trainer = trainerService.getTrainerById(trainerId);
        List<TrainerPhotoEntity> photos = trainerService.getTrainerPhotos(trainerId);

        model.addAttribute("trainer", trainer);
        model.addAttribute("photos", photos);

        boolean loggedIn = (userDetails != null);
        model.addAttribute("loggedIn", loggedIn);

        boolean isTrainer = false;
        boolean appliedToThis = false;
        boolean appliedToAny = false;

        if (loggedIn) {
            Long loginUserId = Long.parseLong(userDetails.getUsername());
            log.info("로그인한 사용자 ID: {}", loginUserId);

            // 먼저, 로그인한 사용자의 트레이너 정보 조회
            TrainerEntity loggedInTrainer = trainerService.getTrainerByUserId(loginUserId);
            if (loggedInTrainer != null) {
                // 로그인한 사용자가 트레이너인 경우
                isTrainer = loggedInTrainer.getTrainerId().equals(trainerId);
                // 트레이너인 경우, 버튼은 보이지 않도록 처리할 수 있음
            } else {
                // 일반 사용자인 경우, UserEntity를 통해 이메일 조회
                UserEntity user = userService.getUserById(loginUserId);
                if (user != null) {
                    String userEmail = user.getUserEmail();
                    // 현재 페이지의 트레이너에 대해 이미 신청한 경우
                    appliedToThis = consultationService.isAlreadyApplied(userEmail, trainerId);
                    // 일반 사용자가 이미 어느 트레이너에 신청했는지 확인 (전체 신청 내역 존재 여부)
                    appliedToAny = trainerApplicationRepository.existsByUserUserEmail(userEmail);
                }
            }
        }

        log.info("현재 페이지의 트레이너 ID: {}", trainer.getTrainerId());
        log.info("isTrainer: {}", isTrainer);
        log.info("appliedToThis: {}", appliedToThis);
        log.info("appliedToAny: {}", appliedToAny);

        model.addAttribute("isTrainer", isTrainer);
        model.addAttribute("appliedToThis", appliedToThis);
        model.addAttribute("appliedToAny", appliedToAny);

        return "trainer-detail";
    }

    @GetMapping("/trainerJoin")
    public String trainerJoin(Model model) {
        // 필요시 추가 모델 속성 설정
        return "user/trainerJoin";  // templates/user/trainerJoin.html 파일을 렌더링
    }

    @PostMapping("/trainer/joinProc")
    public String joinProcess(@ModelAttribute TrainerDTO trainerDTO, RedirectAttributes redirectAttributes) {
        try {
            boolean success = trainerService.joinProc(trainerDTO);
            if (success) {
                return "redirect:/trainer/joinPending"; // 가입 성공 시 이동
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "이미 등록된 이메일 또는 전화번호입니다!");
                return "redirect:/trainerJoin"; // 가입 실패 시 이동
            }
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "회원가입 처리 중 오류가 발생했습니다.");
            return "redirect:/trainerJoin"; 
        }
    }

    @GetMapping("/trainer/joinPending")
    public String joinPending(Model model) {
        model.addAttribute("message", "가입 신청이 완료되었습니다. 관리자의 승인을 기다려주세요.");
        return "user/pendingTrainer"; // 이 파일을 생성하여 안내 메시지를 표시
    }

	@GetMapping("/trainerschedule")
	public String trainerSchedule(Model model	) {
	    // 필요한 모델 속성이 있다면 여기서 추가 (예: 일정 정보 등)
	    return "schedule/trainerschedule"; // templates 폴더 안에 trainerschedule.html 파일이 있어야 합니다.
	}

	@GetMapping("/trainer/edit")
	public String editTrainerProfile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
	    if (userDetails == null) {
	        return "redirect:/login";
	    }
	    
	    Long loginUserId = Long.parseLong(userDetails.getUsername());
	    TrainerEntity loggedInTrainer = trainerService.getTrainerByUserId(loginUserId);
	    if (loggedInTrainer == null) {
	        return "redirect:/";
	    }
	    
	    model.addAttribute("trainer", loggedInTrainer);
	    return "trainer-edit";
	}

	@PostMapping("/trainer/edit")
	public String updateTrainerProfile(
	        @AuthenticationPrincipal UserDetails userDetails,
	        @RequestParam("trainerId") Long trainerId,
	        @RequestParam("specialization") String specialization,
	        @RequestParam("experience") int experience,
	        @RequestParam("fee") BigDecimal fee,
	        @RequestParam("shortIntro") String shortIntro,
	        @RequestParam("bio") String bio) {
	    if (userDetails == null) {
	        return "redirect:/login";
	    }
	    
	    // userDetails.getUsername()가 사용자 ID 문자열을 반환한다고 가정합니다.
	    Long loginUserId = Long.parseLong(userDetails.getUsername());
	    TrainerEntity loggedInTrainer = trainerService.getTrainerByUserId(loginUserId);
	    
	    // 본인 여부 확인: 폼으로 전달된 trainerId와 로그인한 트레이너의 ID가 일치하는지 체크
	    if (loggedInTrainer == null || !loggedInTrainer.getTrainerId().equals(trainerId)) {
	        return "redirect:/";
	    }
	    
	    // 업데이트 진행
	    loggedInTrainer.setSpecialization(specialization);
	    loggedInTrainer.setExperience(experience);
	    loggedInTrainer.setFee(fee);
	    loggedInTrainer.setShortIntro(shortIntro);
	    loggedInTrainer.setBio(bio);
	    trainerService.saveTrainer(loggedInTrainer);
	    
	    return "redirect:/trainer/" + trainerId;
	}
	
	@PostMapping("/trainer/consultation/apply")
	public String applyForConsultation(
	        @AuthenticationPrincipal LoginUserDetails loginUserDetails,
	        @RequestParam("trainerId") Long trainerId,
	        RedirectAttributes redirectAttributes) {
	    
	    // 로그인 확인: 로그인하지 않았다면 로그인 페이지로 리다이렉트
	    if (loginUserDetails == null) {
	        return "redirect:/user/login";
	    }
	    
	    // 로그인된 사용자 ID와 이메일 가져오기
	    Long userId = loginUserDetails.getUserId();
	    UserEntity user = userService.getUserById(userId);
	    if (user == null) {
	        redirectAttributes.addFlashAttribute("error", "사용자를 찾을 수 없습니다.");
	        return "redirect:/user/login";
	    }
	    String userEmail = user.getUserEmail();
	    
	    // TrainerService를 통해 TrainerEntity 조회
	    TrainerEntity trainer = trainerService.getTrainerById(trainerId);
	    
	    if (trainer == null) {
	        redirectAttributes.addFlashAttribute("error", "트레이너를 찾을 수 없습니다.");
	        return "redirect:/";
	    }
	    
	    // 이미 신청한 상태인지 확인 (로그인된 사용자의 이메일 사용)
	    if (trainerApplicationService.isAlreadyApplied(userEmail, trainerId)) {
	        redirectAttributes.addFlashAttribute("message", "이미 상담 신청하셨습니다.");
	        return "redirect:/trainer/" + trainerId;
	    }
	    
	    // 방문 상담 신청 생성 및 저장
	    trainerApplicationService.createApplication(userId, trainerId);
	    
	    redirectAttributes.addFlashAttribute("message", "방문 상담 신청이 완료되었습니다.");
	    return "redirect:/trainer/" + trainerId;
	}


}
