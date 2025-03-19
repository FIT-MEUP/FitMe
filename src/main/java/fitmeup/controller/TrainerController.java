package fitmeup.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fitmeup.dto.LoginUserDetails;
import fitmeup.dto.TrainerDTO;
import fitmeup.entity.TrainerApplicationEntity;
import fitmeup.entity.TrainerEntity;
import fitmeup.entity.TrainerPhotoEntity;
import fitmeup.entity.UserEntity;
import fitmeup.repository.TrainerApplicationRepository;
import fitmeup.repository.TrainerPhotoRepository;
import fitmeup.repository.TrainerRepository;
import fitmeup.service.FileStorageService;
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
	private final FileStorageService fileStorageService;
	private final TrainerApplicationRepository trainerApplicationRepository;
	private final TrainerPhotoRepository trainerPhotoRepository;
	private final TrainerRepository trainerRepository;

	@GetMapping({ "/", "", "/trainers" })
	public String trainers(@AuthenticationPrincipal LoginUserDetails loginUser, Model model) {
		List<TrainerEntity> trainers = trainerService.getAllTrainers();

		String roles = (loginUser != null) ? checkRoles(loginUser) : "";
		if("Trainer".equals(roles)) {
			Optional<TrainerEntity>  trainer = trainerRepository.findByUser_UserId(loginUser.getUserId());
			
			log.info("trainerId1234: {}", trainer.get().getTrainerId());
			model.addAttribute("trainerId",trainer.get().getTrainerId()) ;
		}
		
		model.addAttribute("roles", roles);
		model.addAttribute("trainers", trainers);
		
		return "trainers";
	}

	public String checkRoles(LoginUserDetails loginUser) {
		String roles = "";
		if (loginUser.getRoles().equals("Trainer")) {
			roles = "Trainer";
		} else {
			Long userId = loginUser.getUserId();
			boolean isNowPt = userService.checkPt(userId);

			if (isNowPt) {
				roles = "UserNowPt";
			} else {
				roles = "UserNotPt";
			}
		}
		return roles;
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
				Optional<TrainerApplicationEntity> applicationOpt = trainerApplicationService
						.getApplicationByUserIdAndTrainerId(loginUserId, trainerId);
				if (applicationOpt.isPresent()) {
					TrainerApplicationEntity application = applicationOpt.get();
					appliedToThis = (application.getStatus() == TrainerApplicationEntity.Status.Pending);
				}
				appliedToAny = trainerApplicationRepository.existsByUserUserIdAndStatusIn(loginUserId,
						List.of(TrainerApplicationEntity.Status.Pending, TrainerApplicationEntity.Status.Approved));
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
		return "user/trainerJoin";
	}

	@PostMapping("/trainer/joinProc")
	public String joinProcess(@ModelAttribute TrainerDTO trainerDTO, RedirectAttributes redirectAttributes) {
		try {
			boolean success = trainerService.joinProc(trainerDTO);
			if (success) {
				return "redirect:/trainer/joinPending";
			} else {
				redirectAttributes.addFlashAttribute("errorMessage", "이미 등록된 이메일 또는 전화번호입니다!");
				return "redirect:/trainerJoin";
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
		return "user/pendingTrainer";
	}

	@GetMapping("/trainerschedule")
	public String trainerSchedule(Model model) {
		return "schedule/trainerschedule";
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
	public String updateTrainerProfile(@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam("trainerId") Long trainerId, @RequestParam("specialization") String specialization,
			@RequestParam("experience") int experience, @RequestParam("fee") BigDecimal fee,
			@RequestParam("shortIntro") String shortIntro, @RequestParam("bio") String bio,
			@RequestParam(value = "profileImages", required = false) List<MultipartFile> profileImages) {

		if (userDetails == null) {
			return "redirect:/login";
		}

		Long loginUserId = Long.parseLong(userDetails.getUsername());
		TrainerEntity loggedInTrainer = trainerService.getTrainerByUserId(loginUserId);

		if (loggedInTrainer == null || !loggedInTrainer.getTrainerId().equals(trainerId)) {
			return "redirect:/";
		}

		// 프로필 정보 업데이트
		loggedInTrainer.setSpecialization(specialization);
		loggedInTrainer.setExperience(experience);
		loggedInTrainer.setFee(fee);
		loggedInTrainer.setShortIntro(shortIntro);
		loggedInTrainer.setBio(bio);
		trainerService.saveTrainer(loggedInTrainer);

		// 새 파일이 업로드된 경우: 기존 사진 삭제 후 새 사진 저장
		if (profileImages != null && !profileImages.isEmpty()) {
			// 기존 사진 삭제 (DB와 파일 시스템)
			List<TrainerPhotoEntity> existingPhotos = trainerService.getTrainerPhotos(trainerId);
			for (TrainerPhotoEntity photo : existingPhotos) {
				try {
					fileStorageService.deleteFile(photo.getPhotoUrl());
				} catch (Exception e) {
					log.error("파일 삭제 실패: {}", e.getMessage());
				}
				trainerPhotoRepository.delete(photo);
			}
			// 새 사진 저장 (빈 파일은 saveTrainerPhotos 메서드에서 건너뜁니다)
			trainerService.saveTrainerPhotos(loggedInTrainer, profileImages);
		}

		return "redirect:/trainer/" + trainerId;
	}

	@PostMapping("/trainer/consultation/apply")
	public String applyForConsultation(@AuthenticationPrincipal LoginUserDetails loginUserDetails,
			@RequestParam("trainerId") Long trainerId, RedirectAttributes redirectAttributes) {

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

		Optional<TrainerApplicationEntity> applicationOpt = trainerApplicationService
				.getApplicationByUserIdAndTrainerId(userId, trainerId);
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
	public String cancelConsultation(@AuthenticationPrincipal LoginUserDetails loginUserDetails,
			@RequestParam("trainerId") Long trainerId, RedirectAttributes redirectAttributes) {

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

	// AJAX를 위한 사진 삭제 엔드포인트 (URL 변경: /api/trainer/photo/delete)
	@PostMapping("/api/trainer/photo/delete")
	@ResponseBody
	public ResponseEntity<?> deleteTrainerPhotoAjax(@RequestParam("photoId") Long photoId,
			@AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		TrainerPhotoEntity photo = trainerPhotoRepository.findById(photoId).orElse(null);
		if (photo == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사진을 찾을 수 없습니다.");
		}
		Long loginUserId = Long.parseLong(userDetails.getUsername());
		if (!photo.getTrainer().getUser().getUserId().equals(loginUserId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
		}
		try {
			fileStorageService.deleteFile(photo.getPhotoUrl());
			trainerPhotoRepository.delete(photo);
			return ResponseEntity.ok("사진이 삭제되었습니다.");
		} catch (Exception e) {
			log.error("파일 삭제 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 삭제 중 오류가 발생했습니다.");
		}
	}
}
