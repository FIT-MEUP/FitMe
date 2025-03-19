package fitmeup.utilcontroller;



import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import fitmeup.dto.LoginUserDetails;
import fitmeup.entity.TrainerEntity;
import fitmeup.repository.TrainerRepository;
import fitmeup.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

	private final UserService userService;
	private final TrainerRepository trainerRepository;

	@ModelAttribute
	public void addGlobalAttributes(Model model, @AuthenticationPrincipal LoginUserDetails loginUser) {
		// 기본적으로 로그인하지 않은 경우 처리
		if (loginUser == null) {
			model.addAttribute("roles", "");
			model.addAttribute("trainerId", null);
			return;
		}

		// 로그인한 사용자의 역할을 확인하여 `roles` 추가
		String roles = checkRoles(loginUser);
		model.addAttribute("roles", roles);

		// `roles`가 'Trainer'인 경우 `trainerId` 추가
		if ("Trainer".equals(roles)) {
			Optional<TrainerEntity> trainer = trainerRepository.findByUser_UserId(loginUser.getUserId());
			trainer.ifPresent(value -> model.addAttribute("trainerId", value.getTrainerId()));
		}
	}

	private String checkRoles(LoginUserDetails loginUser) {
		String roles = "";
		if (loginUser.getRoles().equals("Trainer")) {
			roles = "Trainer";
		} else {
			Long userId = loginUser.getUserId();
			boolean isNowPt = userService.checkPt(userId);
			roles = isNowPt ? "UserNowPt" : "UserNotPt";
		}
		return roles;
	}
}