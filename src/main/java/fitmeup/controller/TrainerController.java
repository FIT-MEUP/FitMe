package fitmeup.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
        Long loggedInTrainerId = null;

        if (loggedIn) {
            Long loginUserId = Long.parseLong(userDetails.getUsername());
            log.info("로그인한 사용자 ID: {}", loginUserId);

            TrainerEntity loggedInTrainer = trainerService.getTrainerByUserId(loginUserId);
            
            if (loggedInTrainer != null) {
                isTrainer = true; // 트레이너 계정이면 true
                loggedInTrainerId = loggedInTrainer.getTrainerId();
            } else {
                // ✅ "Pending" 상태인 경우만 true, "Rejected" 상태는 false
                Optional<TrainerApplicationEntity> applicationOpt = trainerApplicationService.getApplicationByUserIdAndTrainerId(loginUserId, trainerId);
                
                if (applicationOpt.isPresent()) {
                    TrainerApplicationEntity application = applicationOpt.get();
                    appliedToThis = (application.getStatus() == TrainerApplicationEntity.Status.Pending);
                }
                // ✅ 다른 트레이너에게 신청한 기록이 있는지 확인 (Rejected는 제외)
                appliedToAny = trainerApplicationRepository.existsByUserUserIdAndStatusIn(
                    loginUserId, List.of(TrainerApplicationEntity.Status.Pending, TrainerApplicationEntity.Status.Approved)
                );
            }
        }

        model.addAttribute("isTrainer", isTrainer);
        model.addAttribute("loggedInTrainerId", loggedInTrainerId);
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

	    log.info("💡 상담 신청 요청: 로그인 사용자={}, 트레이너={}", loginUserDetails, trainerId);

	    if (loginUserDetails == null) {
	        log.warn("❌ 로그인하지 않은 사용자가 상담 신청 시도!");
	        return "redirect:/user/login";
	    }

	    Long userId = loginUserDetails.getUserId();
	    if (userId == null) {
	        log.error("❌ 로그인 정보에서 userId를 가져올 수 없음!");
	        redirectAttributes.addFlashAttribute("error", "사용자 정보가 올바르지 않습니다.");
	        return "redirect:/trainer/" + trainerId;
	    }

	    log.info("✅ 로그인한 사용자 ID: {}", userId);

	    TrainerEntity trainer = trainerService.getTrainerById(trainerId);
	    if (trainer == null) {
	        log.error("❌ 트레이너 ID {} 정보 없음!", trainerId);
	        redirectAttributes.addFlashAttribute("error", "트레이너 정보를 찾을 수 없습니다.");
	        return "redirect:/trainers";
	    }

	    log.info("✅ 트레이너 정보 확인 완료: {}", trainer.getTrainerId());

	    // ✅ 기존 신청이 있는지 확인
	    Optional<TrainerApplicationEntity> applicationOpt = trainerApplicationService.getApplicationByUserIdAndTrainerId(userId, trainerId);

	    if (applicationOpt.isPresent()) {
	        TrainerApplicationEntity existingApplication = applicationOpt.get();

	        if (existingApplication.getStatus() == TrainerApplicationEntity.Status.Pending) {
	            log.warn("⚠️ 사용자 ID {}가 이미 트레이너 ID {}에 상담 신청함!", userId, trainerId);
	            redirectAttributes.addFlashAttribute("message", "이미 상담 신청한 트레이너입니다.");
	            return "redirect:/trainer/" + trainerId;
	        } else if (existingApplication.getStatus() == TrainerApplicationEntity.Status.Rejected) {
	            log.info("🔄 기존 신청이 거절됨 (Rejected), 새로운 신청으로 업데이트");
	            existingApplication.setStatus(TrainerApplicationEntity.Status.Pending);
	            trainerApplicationService.saveApplication(existingApplication);
	            redirectAttributes.addFlashAttribute("message", "상담 신청이 다시 접수되었습니다.");
	            return "redirect:/trainer/" + trainerId;
	        }
	    }

	    try {
	        trainerApplicationService.createApplication(userId, trainerId);
	        log.info("✅ 상담 신청 완료: userId={}, trainerId={}", userId, trainerId);
	        redirectAttributes.addFlashAttribute("message", "방문 상담 신청이 완료되었습니다.");
	    } catch (Exception e) {
	        log.error("🔥 상담 신청 중 오류 발생: {}", e.getMessage(), e);
	        redirectAttributes.addFlashAttribute("error", "상담 신청 중 오류가 발생했습니다.");
	    }

	    return "redirect:/trainer/" + trainerId;
	}

	@PostMapping("/trainer/consultation/cancel")
	public String cancelConsultation(
	        @AuthenticationPrincipal LoginUserDetails loginUserDetails,
	        @RequestParam("trainerId") Long trainerId,
	        RedirectAttributes redirectAttributes) {

	    if (loginUserDetails == null) {
	        return "redirect:/user/login";
	    }

	    Long userId = loginUserDetails.getUserId();
	    boolean canceled = trainerApplicationService.cancelApplication(userId, trainerId);

	    if (canceled) {
	        redirectAttributes.addFlashAttribute("message", "상담 신청이 취소되었습니다.");
	    } else {
	        redirectAttributes.addFlashAttribute("error", "상담 신청을 취소할 수 없습니다.");
	    }

	    return "redirect:/trainer/" + trainerId;
	}

}
